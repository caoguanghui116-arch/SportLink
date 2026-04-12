package com.mashang.notificationservice.controller;

import com.mashang.notificationservice.domain.entity.R;
import com.mashang.notificationservice.domain.query.create.NotificationQuery;
import com.mashang.notificationservice.domain.vo.NotificationVo;
import com.mashang.notificationservice.service.INotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "消息通知")
@RestController
@RequestMapping("/notification/message")
public class NotificationController {

    @Autowired
    private INotificationService notificationService;

    @ApiOperation("查询用户通知列表")
    @GetMapping("/list/{userId}")
    @ApiImplicitParam(name = "userId", value = "用户ID")
    public R<List<NotificationVo>> listByUserId(@PathVariable Long userId) {
        return R.ok(notificationService.listByUserId(userId));
    }

    @ApiOperation("查询未读通知数")
    @GetMapping("/unread/count/{userId}")
    @ApiImplicitParam(name = "userId", value = "用户ID")
    public R<Integer> getUnreadCount(@PathVariable Long userId) {
        return R.ok(notificationService.getUnreadCount(userId));
    }

    @ApiOperation("标记为已读")
    @PutMapping("/read/{notificationId}")
    @ApiImplicitParam(name = "notificationId", value = "通知ID")
    public R markRead(@PathVariable Long notificationId) {
        return R.toResult(notificationService.markRead(notificationId));
    }

    @ApiOperation("全部标记为已读")
    @PutMapping("/read/all/{userId}")
    @ApiImplicitParam(name = "userId", value = "用户ID")
    public R markAllRead(@PathVariable Long userId) {
        return R.toResult(notificationService.markAllRead(userId));
    }

    @ApiOperation("删除通知")
    @DeleteMapping("/delete/{notificationId}")
    @ApiImplicitParam(name = "notificationId", value = "通知ID")
    public R delete(@PathVariable Long notificationId) {
        return R.toResult(notificationService.delete(notificationId));
    }

    @ApiOperation("发送通知(内部服务调用或管理员)")
    @PostMapping("/send")
    public R send(@RequestBody @Validated NotificationQuery query) {
        return R.toResult(notificationService.send(query));
    }

}
