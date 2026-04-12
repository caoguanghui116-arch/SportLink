package com.mashang.notificationservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.notificationservice.domain.entity.Notification;
import com.mashang.notificationservice.domain.query.create.NotificationQuery;
import com.mashang.notificationservice.domain.vo.NotificationVo;

import java.util.List;

public interface INotificationService extends IService<Notification> {

    /**
     * 发送通知（内部服务调用或管理员）
     * @param query 通知参数
     * @return 返回操作行数
     */
    int send(NotificationQuery query);

    /**
     * 查询用户通知列表（按时间倒序）
     * @param userId 用户ID
     * @return 通知列表
     */
    List<NotificationVo> listByUserId(Long userId);

    /**
     * 查询用户未读通知数量
     * @param userId 用户ID
     * @return 未读数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     * @return 返回操作行数
     */
    int markRead(Long notificationId);

    /**
     * 标记用户所有通知为已读
     * @param userId 用户ID
     * @return 返回操作行数
     */
    int markAllRead(Long userId);

    /**
     * 删除通知
     * @param notificationId 通知ID
     * @return 返回操作行数
     */
    int delete(Long notificationId);

}
