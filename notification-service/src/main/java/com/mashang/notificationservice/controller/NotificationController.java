package com.mashang.notificationservice.controller;

import com.mashang.common.common.R;
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

/**
 * 消息通知控制器
 *
 * <p>提供面向终端用户的消息通知 REST API，涵盖以下核心功能：
 * <ul>
 *   <li>查询用户通知列表 —— 按用户ID分页或全量拉取通知</li>
 *   <li>未读通知数统计 —— 红点/角标展示所需数据</li>
 *   <li>标记已读（单条 & 全部） —— 用户阅读反馈</li>
 *   <li>删除通知 —— 用户自行清理</li>
 *   <li>发送通知 —— 供内部服务（如赛事服务、成绩服务）或管理员后台调用</li>
 * </ul>
 *
 * <p>设计思路：
 * <ul>
 *   <li>接口路径统一挂载在 {@code /notification/message} 下，职责单一，便于网关路由</li>
 *   <li>返回值统一使用 {@link com.mashang.common.common.R} 包装，前端可依据 code/data/message 统一处理</li>
 *   <li>发送接口使用 {@code @Validated} 对 {@link NotificationQuery} 做 JSR-303 校验，避免脏数据落库</li>
 * </ul>
 *
 * @author mashang
 */
@Api(tags = "消息通知")
@RestController
@RequestMapping("/notification/message")
public class NotificationController {

    /** 消息通知业务服务接口，封装通知的增删改查逻辑 */
    @Autowired
    private INotificationService notificationService;

    /**
     * 查询指定用户的所有通知列表
     *
     * <p>前端在消息中心页面装载时调用，按通知时间倒序返回。
     *
     * @param userId 用户ID（路径参数，必填）
     * @return 统一响应体，data 为 {@link NotificationVo} 列表
     */
    @ApiOperation("查询用户通知列表")
    @GetMapping("/user/{userId}")
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public R<List<NotificationVo>> listByUserId(@PathVariable Long userId) {
        return R.ok(notificationService.listByUserId(userId));
    }

    /**
     * 查询指定用户的未读通知数量
     *
     * <p>用于前端导航栏/消息入口角标数字展示，轮询频率建议控制在 30 秒以上以减轻服务压力。
     *
     * @param userId 用户ID（路径参数，必填）
     * @return 统一响应体，data 为未读通知数量（Integer）
     */
    @ApiOperation("查询未读通知数")
    @GetMapping("/user/{userId}/unread-count")
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public R<Integer> getUnreadCount(@PathVariable Long userId) {
        return R.ok(notificationService.getUnreadCount(userId));
    }

    /**
     * 将单条通知标记为已读
     *
     * <p>用户点击某条通知后触发，将通知状态从"未读"更新为"已读"。
     *
     * @param notificationId 通知ID（路径参数，必填）
     * @return 统一响应体，操作成功时 code=200，失败时返回错误信息
     */
    @ApiOperation("标记为已读")
    @PutMapping("/{notificationId}/read")
    @ApiImplicitParam(name = "notificationId", value = "通知ID", required = true)
    public R<Void> markRead(@PathVariable Long notificationId) {
        return R.toResult(notificationService.markRead(notificationId));
    }

    /**
     * 将指定用户的所有通知标记为已读
     *
     * <p>"一键已读"功能入口，将用户所有未读通知批量标记为已读。
     *
     * @param userId 用户ID（路径参数，必填）
     * @return 统一响应体，操作成功时 code=200，失败时返回错误信息
     */
    @ApiOperation("全部标记为已读")
    @PutMapping("/user/{userId}/read-all")
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public R<Void> markAllRead(@PathVariable Long userId) {
        return R.toResult(notificationService.markAllRead(userId));
    }

    /**
     * 删除指定通知
     *
     * <p>用户在消息列表中左滑/长按删除某条通知时调用，物理删除或逻辑删除由 Service 层决定。
     *
     * @param notificationId 通知ID（路径参数，必填）
     * @return 统一响应体
     */
    @ApiOperation("删除通知")
    @DeleteMapping("/{notificationId}")
    @ApiImplicitParam(name = "notificationId", value = "通知ID", required = true)
    public R<Void> delete(@PathVariable Long notificationId) {
        return R.toResult(notificationService.delete(notificationId));
    }

    /**
     * 发送通知（内部服务调用或管理员操作）
     *
     * <p>该接口既可以被管理后台调用，也可以被其他微服务通过 Feign 或 MQ 触发调用，
     * 用于在比赛开始前、成绩录入后、报名成功等场景自动推送通知。
     * 请求体中的 {@link NotificationQuery} 经过 {@code @Validated} 校验，
     * 确保 userId、title、content、type 等必填字段不为空。
     *
     * @param query 通知发送参数对象，包含接收用户、标题、内容、类型等信息
     * @return 统一响应体，发送成功时 code=200，否则返回失败信息
     */
    @ApiOperation("发送通知(内部服务调用或管理员)")
    @PostMapping
    public R<Void> send(@RequestBody @Validated NotificationQuery query) {
        return R.toResult(notificationService.send(query));
    }
}
