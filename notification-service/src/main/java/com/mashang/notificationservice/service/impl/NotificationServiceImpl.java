package com.mashang.notificationservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.notificationservice.common.KeyCommon;
import com.mashang.notificationservice.domain.entity.Notification;
import com.mashang.notificationservice.domain.query.create.NotificationQuery;
import com.mashang.notificationservice.domain.vo.NotificationVo;
import com.mashang.notificationservice.mapper.NotificationMapper;
import com.mashang.notificationservice.mapping.NotificationMapping;
import com.mashang.notificationservice.service.INotificationService;
import com.mashang.notificationservice.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public int send(NotificationQuery query) {
        Notification notification = NotificationMapping.INSTANCE.toEntity(query);
        notification.setIsRead(0L); // 默认未读
        notification.setDelFlag(0L);
        int rows = notificationMapper.insert(notification);

        if (rows > 0) {
            // 更新Redis未读计数（+1）
            String unreadKey = KeyCommon.buildUnreadCountKey(query.getUserId());
            redisUtil.incr(unreadKey, 1);
            // 设置过期时间
            redisUtil.expire(unreadKey, 24 * 60 * 60);
        }
        return rows;
    }

    @Override
    public List<NotificationVo> listByUserId(Long userId) {
        return notificationMapper.selectByUserId(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        // 先从Redis获取
        String unreadKey = KeyCommon.buildUnreadCountKey(userId);
        Object cachedCount = redisUtil.getCacheObject(unreadKey);
        if (cachedCount != null) {
            return Integer.parseInt(cachedCount.toString());
        }

        // Redis没有，从数据库查询
        int count = notificationMapper.countUnread(userId);

        // 写入Redis
        redisUtil.setCacheObject(unreadKey, count);
        redisUtil.expire(unreadKey, 24, TimeUnit.HOURS);

        return count;
    }

    @Override
    public int markRead(Long notificationId) {
        int rows = notificationMapper.updateReadStatus(notificationId);

        if (rows > 0) {
            // 由于不知道userId，需要先从数据库查出来然后更新Redis计数
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification != null) {
                String unreadKey = KeyCommon.buildUnreadCountKey(notification.getUserId());
                // 递减未读计数
                Object cachedCount = redisUtil.getCacheObject(unreadKey);
                if (cachedCount != null && Integer.parseInt(cachedCount.toString()) > 0) {
                    redisUtil.decr(unreadKey, 1);
                } else {
                    // 缓存失效则删除，下次从数据库查
                    redisUtil.deleteObject(unreadKey);
                }
            }
        }
        return rows;
    }

    @Override
    public int markAllRead(Long userId) {
        int rows = notificationMapper.updateAllReadStatus(userId);

        if (rows > 0) {
            // 清除Redis未读计数
            String unreadKey = KeyCommon.buildUnreadCountKey(userId);
            redisUtil.deleteObject(unreadKey);
        }
        return rows;
    }

    @Override
    public int delete(Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new RuntimeException("通知不存在");
        }

        // 如果是未读的通知，需要减少Redis计数
        if (notification.getIsRead() == 0L) {
            String unreadKey = KeyCommon.buildUnreadCountKey(notification.getUserId());
            Object cachedCount = redisUtil.getCacheObject(unreadKey);
            if (cachedCount != null && Integer.parseInt(cachedCount.toString()) > 0) {
                redisUtil.decr(unreadKey, 1);
            } else {
                redisUtil.deleteObject(unreadKey);
            }
        }

        // 逻辑删除
        notification.setDelFlag(1L);
        return notificationMapper.updateById(notification);
    }
}
