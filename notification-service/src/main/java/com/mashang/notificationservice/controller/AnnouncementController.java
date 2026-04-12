package com.mashang.notificationservice.controller;

import com.mashang.notificationservice.domain.entity.R;
import com.mashang.notificationservice.domain.query.create.AnnouncementQuery;
import com.mashang.notificationservice.domain.vo.AnnouncementVo;
import com.mashang.notificationservice.service.IAnnouncementService;
import com.mashang.notificationservice.utils.JWTUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "公告管理")
@RestController
@RequestMapping("/notification/announcement")
public class AnnouncementController {

    @Autowired
    private IAnnouncementService announcementService;

    @ApiOperation("发布公告")
    @PostMapping("/publish")
    public R publish(HttpServletRequest request, @RequestBody @Validated AnnouncementQuery query) {
        // 从JWT中获取userId作为发布者ID
        Long userId = JWTUtil.getUserId(request);
        if (userId == 0L) {
            return R.fail("未登录或登录已过期");
        }
        // 通过请求头中的roleId校验管理员权限
        String roleIdHeader = request.getHeader("roleId");
        if (roleIdHeader == null || !"1".equals(roleIdHeader)) {
            return R.fail(403, "无管理员权限");
        }
        return R.toResult(announcementService.publish(query, userId));
    }

    @ApiOperation("修改公告")
    @PutMapping("/update")
    @ApiImplicitParam(name = "announcementId", value = "公告ID", required = true)
    public R update(@RequestParam Long announcementId, @RequestBody @Validated AnnouncementQuery query) {
        if (announcementService.detail(announcementId) == null) {
            return R.fail("公告不存在");
        }
        return R.toResult(announcementService.update(announcementId, query));
    }

    @ApiOperation("删除公告")
    @DeleteMapping("/delete/{id}")
    @ApiImplicitParam(name = "id", value = "公告ID")
    public R delete(@PathVariable("id") Long announcementId) {
        return R.toResult(announcementService.delete(announcementId));
    }

    @ApiOperation("查询公告列表")
    @GetMapping("/list")
    public R<List<AnnouncementVo>> list() {
        return R.ok(announcementService.listAll());
    }

    @ApiOperation("查询运动会公告")
    @GetMapping("/list/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会ID")
    public R<List<AnnouncementVo>> listByMeetingId(@PathVariable Long meetingId) {
        return R.ok(announcementService.listByMeetingId(meetingId));
    }

    @ApiOperation("公告详情")
    @GetMapping("/detail/{id}")
    @ApiImplicitParam(name = "id", value = "公告ID")
    public R<AnnouncementVo> detail(@PathVariable("id") Long announcementId) {
        return R.ok(announcementService.detail(announcementId));
    }

    @ApiOperation("发布草稿公告")
    @PutMapping("/publish/{id}")
    @ApiImplicitParam(name = "id", value = "公告ID")
    public R publishDraft(@PathVariable("id") Long announcementId) {
        return R.toResult(announcementService.publishDraft(announcementId));
    }

}
