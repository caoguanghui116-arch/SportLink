package com.mashang.eventservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mashang.eventservice.domain.entity.EventItem;
import com.mashang.eventservice.domain.entity.R;
import com.mashang.eventservice.domain.query.create.BasicSetupQuery;
import com.mashang.eventservice.domain.query.create.EventItemQuery;
import com.mashang.eventservice.domain.query.update.EventItemUpdate;
import com.mashang.eventservice.mapping.EventItemMapping;
import com.mashang.eventservice.service.IEventItemService;
import com.mashang.eventservice.service.ISportsMeetingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "管理端--基础配置")
@RestController
@RequestMapping("/basic/setup")
public class BasicSetupController {

    @Autowired
    private ISportsMeetingService sportsMeetingService;

    @Autowired
    private IEventItemService eventItemService;

    @ApiOperation("赛事信息设置")
    @PostMapping("/add/meeting")
    public R addMeeting(@RequestBody @Validated BasicSetupQuery addQuery){

        return R.toResult(sportsMeetingService.addMeeting(addQuery));
    }

    @ApiOperation("项目信息管理--添加")
    @PostMapping("/add/project")
    public R addProject(@RequestBody @Validated EventItemQuery addQuery){

        return R.toResult(eventItemService.addProject(addQuery));
    }

    @ApiOperation("项目信息管理--修改")
    @PutMapping("/update/project")
    public R updateProject(@RequestBody @Validated EventItemUpdate updateQuery){

        return R.toResult(eventItemService.updateProject(updateQuery));
    }

    @ApiOperation("项目信息管理--删除")
    @DeleteMapping("/delete/{itemId}")
    @ApiImplicitParam(name = "itemId",value = "项目id")
    public R deleteProject(@PathVariable Long itemId){

        return R.toResult(eventItemService.deleteProject(itemId));
    }

    @ApiOperation("查询项目详情")
    @GetMapping("/item/{itemId}")
    public R getItemInfo(@PathVariable Long itemId) {
        return R.ok(eventItemService.getById(itemId));
    }

    @ApiOperation("查询所有赛事")
    @GetMapping("/all/meeting")
    public R allMeeting() {
        return R.ok(sportsMeetingService.list());
    }
}
