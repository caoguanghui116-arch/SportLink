package com.mashang.scoreservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.common.constants.CacheConstants;
import com.mashang.scoreservice.domain.entity.TeamResult;
import com.mashang.scoreservice.domain.query.create.TeamResultQuery;
import com.mashang.scoreservice.domain.vo.TeamResultVo;
import com.mashang.scoreservice.mapper.TeamResultMapper;
import com.mashang.scoreservice.mapping.ScoreMapping;
import com.mashang.scoreservice.service.ITeamResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 团体成绩服务实现类
 * <p>
 * 核心职责：处理团体（队伍）成绩的录入、查询和排名重算逻辑。
 * 设计思路：
 * <ul>
 *   <li><b>Upsert 策略</b>：录入成绩时先查询是否存在该队伍在该项目的已有成绩，
 *       存在则更新分数，不存在则插入新记录。这种"先查后判"的模式避免了重复插入，
 *       同时保证了成绩的可修改性（裁判可以更正录入错误的成绩）</li>
 *   <li><b>排名重算</b>：每次成绩变更后自动触发该项目下所有队伍按分数降序重新排名，
 *       保证排名数据的实时性和准确性</li>
 *   <li><b>缓存策略</b>：查询采用 Cache Aside 模式，先查 Redis 缓存，未命中则查数据库并回写缓存；
 *       写入时主动删除对应缓存，等待下一次查询时重新加载，避免缓存与数据库数据不一致</li>
 * </ul>
 *
 * @author SportLink Team
 */
@Service
public class TeamResultServiceImpl extends ServiceImpl<TeamResultMapper, TeamResult> implements ITeamResultService {

    /**
     * Redis 缓存 Key 前缀：团体成绩按项目缓存
     * 完整 Key 格式：team:result:item:{itemId}
     * 存储内容：List<TeamResultVo> 该项目的所有团体成绩列表（含排名）
     */
    private static final String TEAM_RESULT_ITEM_PREFIX = "team:result:item:";

    /** 团体成绩数据访问层，负责自定义 SQL 查询（按项目ID查询成绩列表） */
    @Autowired
    private TeamResultMapper teamResultMapper;

    /** Redis 操作模板，用于缓存读写和删除操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 录入团体成绩（Upsert 逻辑）
     * <p>
     * 核心业务流程：
     * <ol>
     *   <li>根据 teamEntryId + itemId + meetingId 三个维度联合查询是否已有成绩记录</li>
     *   <li><b>存在则更新</b>：通过 updateWrapper 仅更新 score 字段，保持其他字段不变</li>
     *   <li><b>不存在则插入</b>：通过 MapStruct 转换 Query 为 Entity，设置状态为1（正常），然后插入</li>
     *   <li>成绩变更后触发该项目排名重算</li>
     *   <li>写入成功后删除该项目对应的 Redis 缓存</li>
     * </ol>
     * <p>
     * 使用场景：裁判在比赛结束后录入或更正某支队伍的比赛成绩。
     *
     * @param query 团体成绩录入请求体，包含 teamEntryId（队伍报名ID）、itemId（项目ID）、
     *              meetingId（运动会ID）、score（成绩值）
     * @return 受影响行数，>0 表示操作成功
     */
    @Override
    public int entry(TeamResultQuery query) {
        // 构建查询条件：按队伍报名ID + 项目ID + 运动会ID 唯一确定一条团体成绩记录
        LambdaQueryWrapper<TeamResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamResult::getTeamEntryId, query.getTeamEntryId())
                .eq(TeamResult::getItemId, query.getItemId())
                .eq(TeamResult::getMeetingId, query.getMeetingId());

        // 执行查询，判断是否已有成绩记录
        TeamResult existing = teamResultMapper.selectOne(wrapper);

        int result;
        if (existing != null) {
            // ---- Upsert 分支一：记录已存在，执行更新 ----
            LambdaUpdateWrapper<TeamResult> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TeamResult::getTeamResultId, existing.getTeamResultId())
                    .set(TeamResult::getScore, query.getScore());
            result = teamResultMapper.update(null, updateWrapper);
        } else {
            // ---- Upsert 分支二：记录不存在，执行插入 ----
            // 使用 MapStruct 将请求参数转换为实体对象
            TeamResult entity = ScoreMapping.INSTANCE.toEntity(query);
            entity.setStatus(1L);  // 状态设为1：正常（0=无效，1=有效，2=已删除）
            result = teamResultMapper.insert(entity);
        }

        // 成绩变更后立即重算该项目的所有队伍排名
        recalculateTeamRanking(query.getItemId());

        // Cache Aside 模式：写入/更新数据后删除缓存，保证下次查询拿到最新数据
        if (result > 0) {
            redisTemplate.delete(TEAM_RESULT_ITEM_PREFIX + query.getItemId());
        }
        return result;
    }

    /**
     * 按项目ID查询团体成绩列表（带 Redis 缓存）
     * <p>
     * 采用 <b>Cache Aside（旁路缓存）模式</b>：
     * <ol>
     *   <li>先查 Redis 缓存，命中则直接返回</li>
     *   <li>缓存未命中则查询数据库</li>
     *   <li>将数据库查询结果回写到 Redis，设置 10 分钟过期时间</li>
     * </ol>
     * <p>
     * 使用场景：前端查看某个团体项目（如拔河比赛）的所有队伍成绩和排名。
     *
     * @param itemId 项目ID，唯一标识一个比赛项目
     * @return 该项目下所有团体成绩的 VO 列表，按分数降序排列
     */
    @Override
    public List<TeamResultVo> listByItemId(Long itemId) {
        String key = TEAM_RESULT_ITEM_PREFIX + itemId;
        // Cache Aside 模式：第一步，先查缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (List<TeamResultVo>) cached;
        }
        // Cache Aside 模式：第二步，缓存未命中，查询数据库
        List<TeamResultVo> list = teamResultMapper.selectByItemId(itemId);
        // Cache Aside 模式：第三步，将查询结果写入缓存，设置过期时间防止缓存常驻
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(key, list, 10, TimeUnit.MINUTES);
        }
        return list;
    }

    /**
     * 重新计算团体项目排名
     * <p>
     * 核心逻辑：
     * <ol>
     *   <li>查询该项目下所有队伍的成绩（已在 Mapper 层按 score 降序排列）</li>
     *   <li>遍历排序后的列表，按顺序分配排名（第1名 rank=1，第2名 rank=2，以此类推）</li>
     *   <li>使用 LambdaUpdateWrapper 按 teamResultId 逐条更新 rank 字段</li>
     * </ol>
     * <p>
     * 注意：此方法在当前类中是 private 的，仅在 entry() 方法中成绩变更时被调用。
     * 排名规则是分数越高排名越靠前（降序排名）。
     *
     * @param itemId 项目ID，需要重算排名的比赛项目
     */
    private void recalculateTeamRanking(Long itemId) {
        // Mapper 层查询已按 score DESC 排序，因此列表顺序即为排名顺序
        List<TeamResultVo> resultList = teamResultMapper.selectByItemId(itemId);
        if (resultList != null && !resultList.isEmpty()) {
            for (int i = 0; i < resultList.size(); i++) {
                TeamResultVo vo = resultList.get(i);
                // 构建更新条件：按主键 teamResultId 精确定位
                LambdaUpdateWrapper<TeamResult> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(TeamResult::getTeamResultId, vo.getTeamResultId())
                        .set(TeamResult::getRank, (long) (i + 1));  // 列表索引从0开始，排名从1开始
                teamResultMapper.update(null, updateWrapper);
            }
        }
    }
}
