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
 * 赛事项目管理 —— 含 Cache-Aside 缓存策略。
 *
 * 缓存策略说明（Cache-Aside 模式）：
 * 1. 查询：先查 Redis → 命中直接返回 → 未命中查 DB → 写入 Redis
 * 2. 更新/删除：先更新 DB → 再删除 Redis 缓存（下次查询时重新加载）
 * 3. 缓存穿透防护：DB 为空时写入短 TTL 的 NULL 哨兵值
 *
 * 为什么不用 @Cacheable 注解：
 * - 需要精细控制 TTL（不同场景不同过期时间）
 * - 需要缓存穿透保护（Spring Cache 不内置）
 * - 需要条件缓存（空值不缓存/短 TTL 缓存）
 */
@Service
public class EventItemServiceImpl extends ServiceImpl<EventItemMapper, EventItem> implements IEventItemService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EventItemMapper eventItemMapper;

    /**
     * 新增项目 —— 写 DB 后清除缓存（下次查询重新加载全量列表）
     */
    @Override
    public int addProject(EventItemQuery addQuery) {
        // 项目名称唯一性校验
        LambdaQueryWrapper<EventItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventItem::getItemName, addQuery.getItemName());
        if (eventItemMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("项目名称已经存在");
        }

        int rows = eventItemMapper.insert(EventItemMapping.INSTANCE.toEntity(addQuery));
        // 写 DB 成功后清除缓存 —— 避免返回过期数据
        if (rows > 0) {
            redisTemplate.delete(KeyCommon.buildKey());
        }
        return rows;
    }

    /**
     * 更新项目 —— 写 DB 后清除缓存
     */
    @Override
    public int updateProject(EventItemUpdate updateQuery) {
        LambdaUpdateWrapper<EventItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(EventItem::getItemId, updateQuery.getItemId())
                .setEntity(EventItemMapping.INSTANCE.toEntity(updateQuery));

        int rows = eventItemMapper.update(null, wrapper);
        if (rows > 0) {
            redisTemplate.delete(KeyCommon.buildKey());
        }
        return rows;
    }

    /**
     * 删除项目 —— 写 DB 后清除缓存
     */
    @Override
    public int deleteProject(Long itemId) {
        int rows = eventItemMapper.deleteById(itemId);
        if (rows > 0) {
            redisTemplate.delete(KeyCommon.buildKey());
        }
        return rows;
    }

    /**
     * 查询所有项目列表 —— 完整 Cache-Aside 模式。
     *
     * 缓存 Key：list:event:all
     * 缓存 TTL：30 分钟（CacheConstants.EVENT_TTL）
     */
    @Override
    public List<EventItemVo> allItem() {
        String cacheKey = KeyCommon.buildKey();

        // ---- 第1步：查 Redis 缓存 ----
        List<EventItemVo> cacheList = (List<EventItemVo>) redisTemplate.opsForValue().get(cacheKey);
        if (cacheList != null) {
            return cacheList;  // 缓存命中，直接返回
        }

        // ---- 第2步：缓存未命中 → 查数据库 ----
        List<EventItemVo> eventItemVos = eventItemMapper.allItem();

        // ---- 第3步：防止缓存穿透（数据库也没有的情况） ----
        // 写入一个短 TTL 的 NULL 哨兵，防止攻击者/异常流量持续穿透到 DB
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
