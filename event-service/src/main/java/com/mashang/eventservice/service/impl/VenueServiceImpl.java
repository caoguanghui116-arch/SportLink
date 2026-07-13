package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.common.constants.CacheConstants;
import com.mashang.eventservice.domain.entity.Venue;
import com.mashang.eventservice.domain.vo.VenueVo;
import com.mashang.eventservice.mapper.VenueMapper;
import com.mashang.eventservice.service.IVenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 场地服务实现类
 * <p>
 * 核心职责：负责场地（Venue）数据的查询和缓存管理，采用 Cache-Aside 缓存策略。
 * <p>
 * 业务背景：场地是赛程编排的基础资源，包括篮球场、足球场、游泳馆等。
 * 场地数据相对稳定，变动频率低，非常适合缓存以减轻数据库压力。
 * <p>
 * 缓存策略（Cache-Aside 模式）：
 * <ol>
 *   <li><b>查询流程</b>：先查 Redis → 命中直接返回 → 未命中查 DB → 结果写入 Redis（TTL 30 分钟）</li>
 *   <li><b>缓存失效</b>：提供 evictVenueCache() 方法供外部调用，在场地数据变更时主动清除缓存</li>
 * </ol>
 * <p>
 * 缓存 Key 设计：venue:all —— 全量场地数据使用单一 Key，简化缓存管理。
 * 场地数据量通常不大（几十到几百条），全量缓存是合理的选择。
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，自动获得基础 CRUD 能力。
 *
 * @author mashang
 */
@Service
public class VenueServiceImpl extends ServiceImpl<VenueMapper, Venue> implements IVenueService {

    /**
     * 场地全量缓存 Key
     * 由于场地数据量通常不大，使用单一 Key 缓存全量场地列表
     */
    private static final String VENUE_ALL_KEY = "venue:all";

    /** 场地 Mapper —— 用于自定义数据库操作（如查询场地 VO 列表） */
    @Autowired
    private VenueMapper venueMapper;

    /** Redis 模板 —— 用于 Cache-Aside 缓存读写操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询所有场地列表
     * <p>
     * 采用 Cache-Aside 模式：
     * <ol>
     *   <li><b>步骤1 - 查缓存</b>：从 Redis 读取 key = venue:all</li>
     *   <li><b>步骤2 - 缓存命中</b>：直接返回反序列化后的场地 VO 列表</li>
     *   <li><b>步骤3 - 缓存未命中</b>：从 DB 查询所有场地（调用自定义 Mapper 的 allVenue 方法）</li>
     *   <li><b>步骤4 - 回写缓存</b>：将结果写入 Redis，TTL 30 分钟</li>
     * </ol>
     * <p>
     * 使用场景：
     * <ul>
     *   <li>赛程编排页面：下拉选择比赛场地</li>
     *   <li>场地管理页面：展示场地列表</li>
     * </ul>
     *
     * @return 所有场地的 VO 列表，包含场地编号、名称、容量、状态等核心信息
     */
    @Override
    public List<VenueVo> allVenue() {
        // Cache Aside 模式：先查 Redis 缓存
        Object cached = redisTemplate.opsForValue().get(VENUE_ALL_KEY);
        if (cached != null) {
            return (List<VenueVo>) cached; // 缓存命中，直接返回
        }

        // 缓存未命中 → 查询数据库
        // 调用自定义 Mapper 方法获取场地 VO 列表（可能包含关联查询）
        List<VenueVo> list = venueMapper.allVenue();

        // 回写 Redis 缓存，TTL 30 分钟
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(VENUE_ALL_KEY, list, 30, TimeUnit.MINUTES);
        }
        return list;
    }

    /**
     * 清除场地缓存
     * <p>
     * 供外部调用（如场地增删改操作后）手动清除 Redis 中的场地缓存，
     * 确保下次查询能加载最新的场地数据。
     * <p>
     * 调用时机：
     * <ul>
     *   <li>新增场地后</li>
     *   <li>修改场地信息后</li>
     *   <li>删除场地后</li>
     * </ul>
     */
    public void evictVenueCache() {
        redisTemplate.delete(VENUE_ALL_KEY);
    }
}
