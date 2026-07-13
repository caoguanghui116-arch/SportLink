package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.eventservice.domain.entity.ScoreRule;
import com.mashang.eventservice.mapper.ScoreRuleMapper;
import com.mashang.eventservice.service.IScoreRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 积分规则服务实现类
 * <p>
 * 核心职责：负责运动会积分规则（ScoreRule）的增删改查，采用 Cache-Aside 缓存策略。
 * <p>
 * 业务背景：运动会中不同名次对应不同的积分（如第一名 10 分、第二名 8 分、第三名 6 分），
 * 积分规则作为基础配置数据，变动频率很低（通常只在赛前配置一次），非常适合使用缓存。
 * <p>
 * 缓存策略（Cache-Aside 模式）：
 * <ol>
 *   <li><b>查询</b>：先查 Redis → 命中返回 → 未命中查 DB → 回写 Redis（TTL 30 分钟）</li>
 *   <li><b>新增/更新</b>：先写 DB → 成功后删除对应 meetingId 维度的 Redis 缓存</li>
 *   <li><b>删除</b>：先查 DB 获取 meetingId → 执行 DELETE → 成功后清除对应缓存</li>
 * </ol>
 * <p>
 * 缓存 Key 设计：event:score_rule:meeting:{meetingId}
 * 以 meetingId 为维度，不同运动的积分规则独立缓存，互不影响。
 * <p>
 * 特别注意：删除操作需要先查后删，因为仅凭 ruleId 无法知道 meetingId，
 * 必须先从 DB 查出记录才能拼接缓存的 Key 进行删除。
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，自动获得基础 CRUD 能力。
 *
 * @author mashang
 */
@Service
public class ScoreRuleServiceImpl extends ServiceImpl<ScoreRuleMapper, ScoreRule> implements IScoreRuleService {

    /**
     * 积分规则缓存 Key 前缀
     * 完整 Key 格式：event:score_rule:meeting:{meetingId}
     * 示例：event:score_rule:meeting:1001
     */
    private static final String SCORE_RULE_MEETING_PREFIX = "event:score_rule:meeting:";

    /** 积分规则 Mapper —— 用于自定义数据库操作 */
    @Autowired
    private ScoreRuleMapper scoreRuleMapper;

    /** Redis 模板 —— 用于 Cache-Aside 缓存读写操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 新增积分规则
     * <p>
     * 执行流程：
     * <ol>
     *   <li><b>唯一性校验</b>：同一运动会下，该名次的积分规则是否已存在</li>
     *   <li><b>写入 DB</b>：执行 INSERT 操作</li>
     *   <li><b>删除缓存</b>：清除该运动会对应的积分规则缓存</li>
     * </ol>
     * <p>
     * 唯一性校验说明：使用 meetingId + rank 组合条件查询，
     * 确保同一运动会下每个名次（如第一名、第二名）只有一条规则，
     * 不同运动会可以有各自独立的名次规则。
     *
     * @param scoreRule 积分规则实体对象，需包含 meetingId（所属运动会）、rank（名次）、score（积分值）
     * @return 受影响的行数，大于 0 表示新增成功
     * @throws RuntimeException 当该运动会下该名次的规则已存在时抛出
     */
    @Override
    public int add(ScoreRule scoreRule) {
        // 唯一性校验：同一运动会下同一名次的规则不可重复
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreRule::getMeetingId, scoreRule.getMeetingId())
                .eq(ScoreRule::getRank, scoreRule.getRank());
        if (scoreRuleMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("该名次的积分规则已存在");
        }

        // 执行 INSERT
        int result = scoreRuleMapper.insert(scoreRule);

        // Cache-Aside：写 DB 成功后删除该运动会维度的缓存
        if (result > 0) {
            redisTemplate.delete(SCORE_RULE_MEETING_PREFIX + scoreRule.getMeetingId());
        }
        return result;
    }

    /**
     * 更新积分规则
     * <p>
     * 根据规则 ID（ruleId）更新积分规则（通常是修改积分值）。
     * 使用 MyBatis-Plus 内置的 updateById 方法，根据主键更新。
     * 更新成功后清除对应运动会的 Redis 缓存。
     *
     * @param scoreRule 积分规则实体对象，必须包含 ruleId（主键）用于定位记录
     * @return 受影响的行数，大于 0 表示更新成功
     */
    @Override
    public int update(ScoreRule scoreRule) {
        // 根据主键 ID 更新（MyBatis-Plus 内置方法）
        int result = scoreRuleMapper.updateById(scoreRule);

        // Cache-Aside：写 DB 成功后删除缓存
        if (result > 0) {
            redisTemplate.delete(SCORE_RULE_MEETING_PREFIX + scoreRule.getMeetingId());
        }
        return result;
    }

    /**
     * 删除积分规则
     * <p>
     * 执行流程：
     * <ol>
     *   <li><b>先查 DB</b>：根据 ruleId 查询积分规则，获取 meetingId（用于后续缓存删除）</li>
     *   <li><b>执行删除</b>：根据 ruleId 物理删除记录</li>
     *   <li><b>删除缓存</b>：根据查到的 meetingId 拼接缓存 Key 后删除</li>
     * </ol>
     * <p>
     * 设计要点：为什么不直接拼缓存 Key？因为仅凭 ruleId 无法知道 meetingId，
     * 必须先查出记录才能获取 meetingId，进而构造正确的缓存 Key。
     *
     * @param ruleId 积分规则 ID（主键）
     * @return 受影响的行数，大于 0 表示删除成功
     */
    @Override
    public int delete(Long ruleId) {
        // 先查询：获取 meetingId，用于后续缓存删除
        ScoreRule scoreRule = scoreRuleMapper.selectById(ruleId);

        // 物理删除：DELETE FROM score_rule WHERE rule_id = ?
        int result = scoreRuleMapper.deleteById(ruleId);

        // Cache-Aside：删除 DB 和缓存都成功时才清除缓存
        if (result > 0 && scoreRule != null) {
            redisTemplate.delete(SCORE_RULE_MEETING_PREFIX + scoreRule.getMeetingId());
        }
        return result;
    }

    /**
     * 根据运动会 ID 查询积分规则列表
     * <p>
     * 采用完整的 Cache-Aside 模式：
     * <ol>
     *   <li><b>步骤1 - 查缓存</b>：从 Redis 读取 key = event:score_rule:meeting:{meetingId}</li>
     *   <li><b>步骤2 - 缓存命中</b>：直接返回反序列化后的规则列表</li>
     *   <li><b>步骤3 - 缓存未命中</b>：从 DB 查询该运动会下所有有效规则</li>
     *   <li><b>步骤4 - 回写缓存</b>：将结果写入 Redis，TTL 30 分钟</li>
     * </ol>
     * <p>
     * 查询条件：
     * <ul>
     *   <li>meetingId 精确匹配</li>
     *   <li>delFlag = 0（过滤逻辑删除的记录）</li>
     * </ul>
     * 排序依据：按 rank 升序排列（第一名在前），便于前端按名次顺序展示积分规则。
     *
     * @param meetingId 运动会 ID
     * @return 该运动会下的积分规则列表，按名次（rank）升序排列
     */
    @Override
    public List<ScoreRule> listByMeetingId(Long meetingId) {
        // 构建缓存 Key：event:score_rule:meeting:{meetingId}
        String key = SCORE_RULE_MEETING_PREFIX + meetingId;

        // ---- 第1步：查 Redis 缓存 ----
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (List<ScoreRule>) cached; // 缓存命中，直接返回
        }

        // ---- 第2步：缓存未命中 → 查数据库 ----
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreRule::getMeetingId, meetingId)   // 按运动会 ID 过滤
                .eq(ScoreRule::getDelFlag, 0)            // 过滤已逻辑删除的记录
                .orderByAsc(ScoreRule::getRank);          // 按名次升序排列（第1名在前）

        List<ScoreRule> list = scoreRuleMapper.selectList(wrapper);

        // ---- 第3步：回写 Redis 缓存 ----
        // TTL 30 分钟：积分规则配置后几乎不会变动，较长 TTL 提升缓存效率
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(key, list, 30, TimeUnit.MINUTES);
        }
        return list;
    }
}
