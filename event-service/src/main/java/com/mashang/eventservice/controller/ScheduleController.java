package com.mashang.eventservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mashang.eventservice.constant.HttpStatus;
import com.mashang.eventservice.domain.entity.R;
import com.mashang.eventservice.domain.query.create.BasicSetupQuery;
import com.mashang.eventservice.domain.query.create.EventItemQuery;
import com.mashang.eventservice.domain.query.create.ScheduleQuery;
import com.mashang.eventservice.domain.query.select.SchedulePageQuery;
import com.mashang.eventservice.domain.vo.RefereeVo;
import com.mashang.eventservice.domain.vo.ScheduleVo;
import com.mashang.eventservice.domain.vo.VenueVo;
import com.mashang.eventservice.feign.RefereeServiceFeign;
import com.mashang.eventservice.service.IEventItemService;
import com.mashang.eventservice.service.IScheduleService;
import com.mashang.eventservice.service.IVenueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mashang.common.common.PageQuery;
import com.mashang.common.common.TableDataInfo;

import java.util.List;

@Api(tags = "管理端--赛事安排——>赛程安排")
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private IVenueService venueService;

    @Autowired
    private IEventItemService eventItemService;

    @Autowired
    private RefereeServiceFeign refereeServiceFeign;

    @Autowired
    private IScheduleService scheduleService;

    @GetMapping("/all/venue")
    @ApiOperation("所有场地")
    public R<List<VenueVo>> allVenue(){

        return R.ok(venueService.allVenue());
    }

    @GetMapping("/all/item")
    @ApiOperation("所有项目")
    public R allItem(){

        return R.ok(eventItemService.allItem());
    }

    @GetMapping("/referee/all")
    @ApiOperation("查询所有裁判")
    public R<List<RefereeVo>> allReferee(){

        return refereeServiceFeign.allReferee();
    }

    @ApiOperation("添加赛程")
    @PostMapping("/add")
    public R addSchedule(@RequestBody @Validated  ScheduleQuery scheduleQuery){

        return R.toResult(scheduleService.addSchedule(scheduleQuery));
    }

    @ApiOperation(value = "赛程信息分页查询",notes = "可以根据比赛时间可以进行区间查询")
    @PostMapping("/page")
    public TableDataInfo<ScheduleVo> page(@Validated PageQuery pageQuery, SchedulePageQuery schedulePageQuery) {

        Page<ScheduleVo> page = scheduleService.page(pageQuery, schedulePageQuery);
        TableDataInfo<ScheduleVo> rspData = new TableDataInfo<>();

        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());

        return rspData;
    }
}
