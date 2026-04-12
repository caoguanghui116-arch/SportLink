package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.eventservice.domain.entity.Schedule;
import com.mashang.eventservice.domain.query.create.ScheduleQuery;
import com.mashang.eventservice.domain.query.select.SchedulePageQuery;
import com.mashang.eventservice.domain.vo.ScheduleVo;
import com.mashang.eventservice.mapper.ScheduleMapper;
import com.mashang.eventservice.mapping.ScheduleMapping;
import com.mashang.eventservice.service.IScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.mashang.common.common.PageQuery;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements IScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public int addSchedule(ScheduleQuery scheduleQuery) {

        return scheduleMapper.insert(ScheduleMapping.INSTANCE.toEntity(scheduleQuery));
    }

    @Override
    public Page<ScheduleVo> page(PageQuery pageQuery, SchedulePageQuery schedulePageQuery) {

        //创建分页对象
        Page<ScheduleVo> page = new Page<>(
                pageQuery.getPageNum(),
                pageQuery.getPageSize()
        );

        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        if (schedulePageQuery.getStartGameTime() != null && schedulePageQuery.getEndGameTime() != null) {
            queryWrapper.between(Schedule::getGameTime, schedulePageQuery.getStartGameTime(),
                    schedulePageQuery.getEndGameTime());
        } else if (schedulePageQuery.getStartGameTime() != null) {
            queryWrapper.ge(Schedule::getGameTime, schedulePageQuery.getStartGameTime());
        } else if (schedulePageQuery.getEndGameTime() != null) {
            queryWrapper.le(Schedule::getGameTime, schedulePageQuery.getEndGameTime());
        }

        return scheduleMapper.page(page,queryWrapper);
    }
}
