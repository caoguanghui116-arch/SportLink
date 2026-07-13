package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.common.constants.CacheConstants;
import com.mashang.eventservice.common.KeyCommon;
import com.mashang.eventservice.domain.entity.EventItem;
import com.mashang.eventservice.domain.query.create.EventItemQuery;
import com.mashang.eventservice.domain.query.update.EventItemUpdate;
import com.mashang.eventservice.domain.vo.EventItemVo;
import com.mashang.eventservice.mapper.EventItemMapper;
import com.mashang.eventservice.mapping.EventItemMapping;
import com.mashang.eventservice.service.IEventItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 赛事项目服务实现类
 * <p>
 * 核心职责：负责比赛项目（EventItem）的增删改查，采用完整的 Cache-Aside 缓存策略。
 * <p>
 * 缓存策略说明（Cache-Aside 模式）：
 * <ol>
 *   <li><b>查询（allItem）</b>：先查 Redis → 命中直接返回 → 未命中查 DB → 写入 Redis</li>
 *   <li><b>更新/删除（addProject / updateProject / deleteProject）</b>：先更新 DB → 再删除 Redis 缓存（下次查询时重新加载）</li>
 *   <li><b>缓存穿透防护</b>：DB 为空时写入短 TTL（2 分钟）的 NULL 哨兵值，防止恶意流量持续穿透到 DB</li>
 * </ol>
 * <p>
 * 为什么不用 Spring @Cacheable 注解：
 * <ul>
 *   <li>需要精细控制 TTL（不同场景不同过期时间）</li>
 *   <li>需要缓存穿透保护（Spring Cache 不内置 NULL 哨兵机制）</li>
 *   <li>需要条件缓存（空值不缓存或短 TTL 缓存）</li>
 * </ul>
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，自动获得基础 CRUD 能力。
 *
 * @author mashang
 */
@Service
public class EventItemServiceImpl extends ServiceImpl<EventItemMapper, EventItem> implements IEventItemService {

    /** Redis 模板 —— 用于 Cache-Aside 缓存读写操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** 比赛项目 Mapper —— 用于自定义数据库操作（如全量查询） */
    @Autowired
    private EventItemMapper eventItemMapper;

    /**
     * 新增比赛项目
     * <p>
     * 执行流程：
     * <ol>
     *   <li>唯一性校验：检查项目名称是否已存在</li>
     *   <li>数据转换：通过 MapStruct 将 Query 对象转换为 Entity 实体</li>
     *   <li>写入 DB：执行 INSERT 操作</li>
     *   <li>删除缓存：写入成功后删除项目列表的 Redis 缓存</li>
     * </ol>
     *
     * @param addQuery 比赛项目创建请求参数，包含 itemName（项目名称）、meetingId（所属运动会）等
     * @return 受影响的行数，大于 0 表示新增成功
     * @throws RuntimeException 当项目名称已存在时抛出
     */
    @Override
    public int addProject(EventItemQuery addQuery) {
        // 项目名称唯一性校验：同一系统中项目名称不可重复
        LambdaQueryWrapper<EventItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventItem::getItemName, addQuery.getItemName());
        if (eventItemMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("项目名称已经存在");
        }

        // MapStruct 转换 DTO → Entity，执行 INSERT
        int rows = eventItemMapper.insert(EventItemMapping.INSTANCE.toEntity(addQuery));

        // Cache-Aside：写 DB 成功后清除缓存，避免返回过期数据
        if (rows > 0) {
            redisTemplate.delete(KeyCommon.buildKey());
        }
        return rows;
    }

    /**
     * 更新比赛项目
     * <p>
     * 使用 LambdaUpdateWrapper 实现动态更新，只更新传入的非空字段。
     * 更新条件：根据 itemId 精确匹配。
     * 更新成功后删除 Redis 缓存，保证下次查询加载最新数据。
     *
     * @param updateQuery 比赛项目更新请求参数，必须包含 itemId
     * @return 受影响的行数，大于 0 表示更新成功
     */
    @Override
    public int updateProject(EventItemUpdate updateQuery) {
        // 构建更新条件：按 itemId 匹配，setEntity 自动映射非空字段
        LambdaUpdateWrapper<EventItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(EventItem::getItemId, updateQuery.getItemId())
                .setEntity(EventItemMapping.INSTANCE.toEntity(updateQuery));

        int rows = eventItemMapper.update(null, wrapper);

        // Cache-Aside：写 DB 成功后删除缓存
        if (rows > 0) {
            redisTemplate.delete(KeyCommon.buildKey());
        }
        return rows;
    }

    /**
     * 删除比赛项目
     * <p>
     * 根据项目 ID 物理删除记录（DELETE 语句，数据不可恢复）。
     * 删除成功后清除 Redis 缓存。
     *
     * @param itemId 比赛项目 ID
     * @return 受影响的行数，大于 0 表示删除成功
     */
    @Override
    public int deleteProject(Long itemId) {
        // 物理删除：直接 DELETE FROM event_item WHERE item_id = ?
        int rows = eventItemMapper.deleteById(itemId);

        // Cache-Aside：写 DB 成功后删除缓存
        if (rows > 0) {
            redisTemplate.delete(KeyCommon.buildKey());
        }
        return rows;
    }

    /**
     * 查询所有比赛项目列表
     * <p>
     * 采用完整的 Cache-Aside 模式 + 缓存穿透保护：
     * <ol>
     *   <li><b>步骤1 - 查缓存</b>：从 Redis 读取 key = list:event:all 的值</li>
     *   <li><b>步骤2 - 缓存命中</b>：直接返回反序列化后的项目列表</li>
     *   <li><b>步骤3 - 缓存未命中</b>：从 DB 通过自定义 SQL 查询所有项目</li>
     *   <li><b>步骤4 - 缓存穿透防护</b>：DB 为空时写入短 TTL（2 分钟）的 NULL 哨兵字符串</li>
     *   <li><b>步骤5 - 正常回写</b>：DB 有数据时写入 Redis，TTL = CacheConstants.EVENT_TTL 分钟</li>
     * </ol>
     * <p>
     * 使用场景：赛程编排页面加载时，下拉选择比赛项目；前端基础配置页面列表展示。
     *
     * @return 所有比赛项目的 VO 列表，包含项目基本信息
     */
    @Override
    public List<EventItemVo> allItem() {
        // 构建缓存 Key（通过 KeyCommon 工具类统一生成）
        String cacheKey = KeyCommon.buildKey();

        // ---- 第1步：查 Redis 缓存 ----
        @SuppressWarnings("unchecked")
        List<EventItemVo> cacheList = (List<EventItemVo>) redisTemplate.opsForValue().get(cacheKey);
        if (cacheList != null) {
            return cacheList; // 缓存命中，直接返回
        }

        // ---- 第2步：缓存未命中 → 查数据库 ----
        // 调用自定义 Mapper 方法，执行关联查询获取完整项目信息
        List<EventItemVo> eventItemVos = eventItemMapper.allItem();

        // ---- 第3步：防止缓存穿透（数据库也没有的情况） ----
        // 写入一个短 TTL 的 NULL 哨兵值，防止攻击者/异常流量持续穿透到 DB
        // 短 TTL（2 分钟）确保数据恢复后能较快加载最新数据
        if (eventItemVos == null || eventItemVos.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, "NULL", 2, TimeUnit.MINUTES);
            return eventItemVos;
        }

        // ---- 第4步：正常写入缓存（设置业务 TTL） ----
        redisTemplate.opsForValue().set(cacheKey, eventItemVos,
                CacheConstants.EVENT_TTL, TimeUnit.MINUTES);
        return eventItemVos;
    }
}
