package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.eventservice.common.KeyCommon;
import com.mashang.eventservice.domain.entity.SportsMeeting;
import com.mashang.eventservice.domain.query.create.BasicSetupQuery;
import com.mashang.eventservice.mapper.SportsMeetingMapper;
import com.mashang.eventservice.mapping.SportsMeetingMapping;
import com.mashang.eventservice.service.ISportsMeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 运动会服务实现类
 * <p>
 * 核心职责：负责运动会（SportsMeeting）的创建和查询，采用 Cache-Aside 缓存策略管理缓存。
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，自动获得基础的 CRUD 能力（insert、deleteById、updateById、selectById 等），
 * 无需手动编写 Mapper XML 即可完成单表操作。
 * <p>
 * 缓存策略（Cache-Aside 模式）：
 * <ol>
 *   <li><b>查询流程</b>：先查 Redis 缓存 → 命中直接返回 → 未命中查 DB → 结果写入 Redis（TTL 30 分钟）</li>
 *   <li><b>写入流程</b>：先写 DB → 成功后删除 Redis 缓存（下次查询自动重建）</li>
 * </ol>
 * 这种模式的优点是：缓存中只存放热点数据，写操作不更新缓存（而是删除），
 * 避免了"先更新 DB 再更新缓存"可能带来的并发问题。
 *
 * @author mashang
 */
@Service
public class SportsMeetingServiceImpl extends ServiceImpl<SportsMeetingMapper, SportsMeeting> implements ISportsMeetingService {

    /** 运动会 Mapper —— 用于自定义数据库操作（如条件查询） */
    @Autowired
    private SportsMeetingMapper sportsMeetingMapper;

    /** Redis 模板 —— 用于 Cache-Aside 缓存读写操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 新增运动会
     * <p>
     * 执行流程：
     * <ol>
     *   <li>唯一性校验：检查届数（meetingSession）和名称（meetingName）是否已存在</li>
     *   <li>数据转换：通过 MapStruct 将 Query 对象转换为 Entity 实体</li>
     *   <li>写入 DB：执行 INSERT 操作</li>
     *   <li>删除缓存：写入成功后删除运动会列表的 Redis 缓存</li>
     * </ol>
     * <p>
     * 唯一性校验说明：届数和名称使用 OR 条件连接，任一重复即判定为冲突，
     * 抛出 RuntimeException 由全局异常处理器统一捕获并返回错误信息给前端。
     *
     * @param addQuery 运动会创建请求参数，包含 meetingSession（届数）、meetingName（名称）等
     * @return 受影响的行数，大于 0 表示新增成功
     * @throws RuntimeException 当届数或名称已存在时抛出
     */
    @Override
    public int addMeeting(BasicSetupQuery addQuery) {
        // 构建查询条件：届数或名称已存在则拒绝
        LambdaQueryWrapper<SportsMeeting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportsMeeting::getMeetingSession, addQuery.getMeetingSession())
                .or()
                .eq(SportsMeeting::getMeetingName, addQuery.getMeetingName());

        // 唯一性校验：已存在则抛出异常
        if (sportsMeetingMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("届数或名称已存在");
        }

        // 通过 MapStruct 转换 DTO → Entity，然后执行 INSERT
        int rows = sportsMeetingMapper.insert(SportsMeetingMapping.INSTANCE.toEntity(addQuery));

        // Cache-Aside：写 DB 成功后删除缓存，确保下次查询加载最新数据
        if (rows > 0) {
            redisTemplate.delete(KeyCommon.buildKey());
        }
        return rows;
    }

    /**
     * 查询所有运动会列表
     * <p>
     * 采用完整的 Cache-Aside 模式：
     * <ol>
     *   <li><b>步骤1 - 查缓存</b>：从 Redis 读取 key = list:meeting:all 的值</li>
     *   <li><b>步骤2 - 缓存命中</b>：直接返回反序列化后的运动会列表</li>
     *   <li><b>步骤3 - 缓存未命中</b>：从 DB 查询所有运动会记录</li>
     *   <li><b>步骤4 - 回写缓存</b>：将查询结果写入 Redis，TTL 为 30 分钟</li>
     * </ol>
     * <p>
     * 使用场景：前端"基础配置"页面加载时调用，展示已有运动会列表供选择或管理。
     * 注意：此处未做缓存穿透保护（空结果不缓存），因为运动会列表几乎不可能为空。
     *
     * @return 所有运动会的列表，如果没有则返回空列表
     */
    @Override
    public List<SportsMeeting> allMeeting() {
        // 构建缓存 Key
        String key = KeyCommon.buildKey();

        // ---- 第1步：查 Redis 缓存 ----
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (List<SportsMeeting>) cached; // 缓存命中，直接返回
        }

        // ---- 第2步：缓存未命中 → 查数据库 ----
        // selectList(null) 查询全表
        List<SportsMeeting> list = sportsMeetingMapper.selectList(null);

        // ---- 第3步：回写 Redis 缓存 ----
        // TTL 30 分钟：平衡数据一致性和缓存命中率
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(key, list, 30, TimeUnit.MINUTES);
        }
        return list;
    }
}
