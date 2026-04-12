package com.mashang.eventservice.controller;

import com.mashang.eventservice.domain.entity.EventCategory;
import com.mashang.eventservice.domain.entity.R;
import com.mashang.eventservice.service.IEventCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端--项目分类")
@RestController
@RequestMapping("/basic/setup/category")
public class EventCategoryController {

    @Autowired
    private IEventCategoryService eventCategoryService;

    @ApiOperation("添加分类")
    @PostMapping("/add")
    public R add(@RequestBody EventCategory category) {
        return R.toResult(eventCategoryService.addCategory(category));
    }

    @ApiOperation("修改分类")
    @PutMapping("/update")
    public R update(@RequestBody EventCategory category) {
        return R.toResult(eventCategoryService.updateById(category));
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/delete/{categoryId}")
    public R delete(@PathVariable Long categoryId) {
        return R.toResult(eventCategoryService.removeById(categoryId));
    }

    @ApiOperation("查询运动会分类列表")
    @GetMapping("/list/{meetingId}")
    public R<List<EventCategory>> list(@PathVariable Long meetingId) {
        return R.ok(eventCategoryService.listByMeetingId(meetingId));
    }
}
