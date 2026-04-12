package com.mashang.eventservice.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.eventservice.domain.entity.Schedule;
import com.mashang.eventservice.domain.query.create.ScheduleQuery;
import com.mashang.eventservice.domain.query.select.SchedulePageQuery;
import com.mashang.eventservice.domain.vo.ScheduleVo;
import com.mashang.common.common.PageQuery;

public interface IScheduleService extends IService<Schedule> {

    /**
     * 赛程信息添加
     * @param scheduleQuery 添加参数
     * @return 返回影响行数
     */
    int addSchedule(ScheduleQuery scheduleQuery);

    /**
     * 赛程安排分页查询
     * @param page 分页对象
     * @param schedulePageQuery 检索条件
     * @return 返回分页集合
     */
    Page<ScheduleVo> page(PageQuery page, SchedulePageQuery schedulePageQuery);
}
