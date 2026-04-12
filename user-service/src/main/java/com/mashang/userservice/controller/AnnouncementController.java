package com.mashang.userservice.controller;

import com.mashang.userservice.domain.entity.Announcement;
import com.mashang.userservice.domain.entity.R;
import com.mashang.userservice.domain.query.create.AnnouncementQuery;
import com.mashang.userservice.service.IAnnouncementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "公告管理")
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private IAnnouncementService announcementService;

    @ApiOperation("发布公告")
    @PostMapping("/publish")
    public R publish(@RequestBody @Validated AnnouncementQuery query, @RequestParam Long publisherId) {
        return R.toResult(announcementService.publish(query, publisherId));
    }

    @ApiOperation("修改公告")
    @PutMapping("/update")
    public R update(@RequestBody Announcement announcement) {
        return R.toResult(announcementService.update(announcement));
    }

    @ApiOperation("删除公告")
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id) {
        return R.toResult(announcementService.delete(id));
    }

    @ApiOperation("公告列表")
    @GetMapping("/list")
    public R<List<Announcement>> list() {
        return R.ok(announcementService.listAll());
    }

    @ApiOperation("运动会公告列表")
    @GetMapping("/list/{meetingId}")
    public R<List<Announcement>> listByMeeting(@PathVariable Long meetingId) {
        return R.ok(announcementService.listByMeetingId(meetingId));
    }

    @ApiOperation("公告详情")
    @GetMapping("/detail/{id}")
    public R<Announcement> detail(@PathVariable Long id) {
        return R.ok(announcementService.detail(id));
    }

    @ApiOperation("发布草稿")
    @PutMapping("/publish/{id}")
    public R publishDraft(@PathVariable Long id) {
        return R.toResult(announcementService.publishDraft(id));
    }
}
