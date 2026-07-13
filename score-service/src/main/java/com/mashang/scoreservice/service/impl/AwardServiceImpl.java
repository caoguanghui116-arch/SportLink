package com.mashang.scoreservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.common.constants.CacheConstants;
import com.mashang.scoreservice.domain.entity.Award;
import com.mashang.scoreservice.domain.query.create.AwardQuery;
import com.mashang.scoreservice.domain.vo.AwardVo;
import com.mashang.scoreservice.mapper.AwardMapper;
import com.mashang.scoreservice.mapping.ScoreMapping;
import com.mashang.scoreservice.service.IAwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 奖项管理服务实现类
 * <p>
 * 核心职责：处理奖项的添加和查询业务逻辑。
 * 设计思路：
 * <ul>
 *   <li><b>数据模型</b>：奖项（Award）是运动会的附属数据，一个运动会可以设置多个奖项，
 *       如"冠军"、"亚军"、"季军"、"最佳运动员"、"精神文明奖"等</li>
 *   <li><b>缓存策略</b>：奖项属于典型的读多写少数据，查询采用 Cache Aside 模式：
 *       先查 Redis 缓存，未命中则查数据库并回写缓存。
 *       写入（添加奖项）后主动删除对应缓存，保证数据一致性</li>
 *   <li><b>过期时间</b>：奖项列表缓存设置 30 分钟过期，比成绩缓存（10分钟）更长，
 *       因为奖项配置变更频率远低于成绩录入</li>
 * </ul>
 *
 * @author SportLink Team
 */
@Service
public class AwardServiceImpl extends ServiceImpl<AwardMapper, Award> implements IAwardService {

    /**
     * Redis 缓存 Key 前缀：奖项按运动会缓存
     * 完整 Key 格式：award:meeting:{meetingId}
     * 存储内容：List<AwardVo> 该运动会的所有奖项配置列表
     */
    private static final String AWARD_MEETING_PREFIX = "award:meeting:";

    /** 奖项数据访问层，负责自定义 SQL 查询（按运动会ID查询奖项列表） */
    @Autowired
    private AwardMapper awardMapper;

    /** Redis 操作模板，用于缓存读写和删除操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 添加奖项
     * <p>
     * 业务流程：
     * <ol>
     *   <li>通过 MapStruct 将前端请求参数（AwardQuery）转换为实体对象（Award）</li>
     *   <li>调用 Mapper 执行数据库插入操作</li>
     *   <li>插入成功后立即删除该运动会对应的 Redis 缓存 Key，
     *       迫使下次查询时从数据库重新加载并回写缓存</li>
     * </ol>
     * <p>
     * 使用场景：管理员在运动会管理后台添加新的奖项类型。
     *
     * @param query 奖项创建请求体，包含 meetingId（运动会ID）、name（奖项名称）、
     *              type（奖项类型）、count（名额数量）
     * @return 受影响行数，>0 表示添加成功
     */
    @Override
    public int add(AwardQuery query) {
        // MapStruct 编译期生成的映射器，将请求参数转换为实体
        Award entity = ScoreMapping.INSTANCE.toEntity(query);
        int result = awardMapper.insert(entity);
        // Cache Aside 模式：写入后清除该运动会的奖项缓存，保证缓存与数据库一致
        if (result > 0) {
            redisTemplate.delete(AWARD_MEETING_PREFIX + query.getMeetingId());
        }
        return result;
    }

    /**
     * 按运动会ID查询奖项列表（带 Redis 缓存）
     * <p>
     * 采用 <b>Cache Aside（旁路缓存）模式</b>：
     * <ol>
     *   <li>先查 Redis 缓存，命中则直接返回，命中率预计在 95% 以上</li>
     *   <li>缓存未命中（首次查询或缓存已过期），查询数据库获取完整列表</li>
     *   <li>将数据库查询结果回写到 Redis，设置 <b>30 分钟</b>过期时间</li>
     * </ol>
     * <p>
     * 缓存过期时间设计考虑：奖项配置变更频率极低（通常运动会开始前配置一次后不再修改），
     * 因此设置较长的过期时间（30分钟），减少数据库查询压力。
     * <p>
     * 使用场景：前端在颁奖页面、奖项展示页面查询该运动会的所有奖项列表。
     *
     * @param meetingId 运动会ID，唯一标识一届运动会
     * @return 该运动会下所有奖项的 VO 列表，可能为空列表
     */
    @Override
    public List<AwardVo> listByMeetingId(Long meetingId) {
        String key = AWARD_MEETING_PREFIX + meetingId;
        // Cache Aside 模式：第一步，先查缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (List<AwardVo>) cached;
        }
        // Cache Aside 模式：第二步，缓存未命中，查询数据库
        List<AwardVo> list = awardMapper.selectByMeetingId(meetingId);
        // Cache Aside 模式：第三步，将结果写入缓存，设置 30 分钟过期（奖项配置读多写少）
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(key, list, 30, TimeUnit.MINUTES);
        }
        return list;
    }
}
