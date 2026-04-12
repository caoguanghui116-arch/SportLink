package com.mashang.eventservice.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mashang.eventservice.domain.entity.Schedule;
import com.mashang.eventservice.domain.vo.ScheduleVo;
import org.apache.ibatis.annotations.Param;

public interface ScheduleMapper extends BaseMapper<Schedule> {

    //赛程安排分页查询
    Page<ScheduleVo> page(@Param("page") Page<ScheduleVo> page, @Param("ew") Wrapper<Schedule> wrapper);
}
