package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.common.constants.CacheConstants;
import com.mashang.eventservice.common.KeyCommon;
import com.mashang.eventservice.domain.entity.Schedule;
import com.mashang.eventservice.domain.query.create.ScheduleQuery;
import com.mashang.eventservice.domain.query.select.SchedulePageQuery;
import com.mashang.eventservice.domain.vo.ScheduleVo;
import com.mashang.eventservice.mapper.ScheduleMapper;
import com.mashang.eventservice.mapping.ScheduleMapping;
import com.mashang.eventservice.service.IScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.mashang.common.common.PageQuery;

import java.util.concurrent.TimeUnit;

/**
 * 赛程管理服务 —— 含 Cache-Aside 缓存策略。
 *
 * 缓存场景：
 * - 赛程分页查询（按运动会ID）：高频读（所有用户查看赛程），低频写（管理员排赛程）
 * - 只在无筛选项（无时间范围）时缓存，带筛选条件时直接查 DB（筛选组合太多，缓存命中率低）
 * - 新增赛程后清除对应运动会的缓存
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements IScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 新增赛程 —— 写 DB 后清除对应运动会的赛程缓存。
     */
    @Override
    public int addSchedule(ScheduleQuery scheduleQuery) {
        int rows = scheduleMapper.insert(ScheduleMapping.INSTANCE.toEntity(scheduleQuery));

        // 新增后清除赛程缓存（按运动会维度）
        if (rows > 0 && scheduleQuery.getMeetingId() != null) {
            redisTemplate.delete(KeyCommon.buildScheduleKey(scheduleQuery.getMeetingId()));
        }
        return rows;
    }

    /**
     * 分页查询赛程 —— 条件缓存（仅无筛选条件时缓存）。
     *
     * 缓存策略：
     * - 无时间筛选 → 查缓存（大部分用户场景）
     * - 有时间筛选 → 直接查 DB（组合太多，缓存意义不大）
     */
    @Override
    public Page<ScheduleVo> page(PageQuery pageQuery, SchedulePageQuery schedulePageQuery) {
        String cacheKey = KeyCommon.buildScheduleKey(schedulePageQuery.getMeetingId());

        // ---- 仅无筛选条件时走缓存 ----
        boolean noFilter = (schedulePageQuery.getStartGameTime() == null
                && schedulePageQuery.getEndGameTime() == null);

        if (noFilter) {
            Page<ScheduleVo> cached = (Page<ScheduleVo>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;  // 缓存命中
            }
        }

        // ---- 构建分页 + 筛选条件查询 ----
        Page<ScheduleVo> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        if (schedulePageQuery.getStartGameTime() != null && schedulePageQuery.getEndGameTime() != null) {
            // 时间范围筛选
            queryWrapper.between(Schedule::getGameTime,
                    schedulePageQuery.getStartGameTime(), schedulePageQuery.getEndGameTime());
        } else if (schedulePageQuery.getStartGameTime() != null) {
            queryWrapper.ge(Schedule::getGameTime, schedulePageQuery.getStartGameTime());
        } else if (schedulePageQuery.getEndGameTime() != null) {
            queryWrapper.le(Schedule::getGameTime, schedulePageQuery.getEndGameTime());
        }

        Page<ScheduleVo> result = scheduleMapper.page(page, queryWrapper);

        // ---- 无需筛选时写入缓存 ----
        if (noFilter && result != null && !result.getRecords().isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, result,
                    CacheConstants.EVENT_TTL, TimeUnit.MINUTES);
        }

        return result;
    }
}
