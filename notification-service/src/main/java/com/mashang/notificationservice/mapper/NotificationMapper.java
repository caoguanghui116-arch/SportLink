package com.mashang.notificationservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.notificationservice.domain.entity.Notification;
import com.mashang.notificationservice.domain.vo.NotificationVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 根据用户ID查询通知列表（按时间倒序）
     */
    List<NotificationVo> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户未读通知数量
     */
    int countUnread(@Param("userId") Long userId);

    /**
     * 标记通知为已读
     */
    int updateReadStatus(@Param("notificationId") Long notificationId);

    /**
     * 标记用户所有通知为已读
     */
    int updateAllReadStatus(@Param("userId") Long userId);

}
