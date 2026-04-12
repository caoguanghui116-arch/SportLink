package com.mashang.notificationservice.common;

import lombok.Data;
import com.mashang.common.constants.CacheConstants;

@Data
public class KeyCommon {
    /**
     * 拼接 Redis Key 方法（避免重复代码）
     */
    public static String buildKey(Long id) {
        return CacheConstants.NOTIFICATION_KEY + id;
    }

    public static String buildKey() {
        return CacheConstants.NOTIFICATION_KEY;
    }

    public static String buildAnnouncementKey() {
        return "announcement:";
    }

    public static String buildAnnouncementKey(Long id) {
        return "announcement:" + id;
    }

    public static String buildUnreadCountKey(Long userId) {
        return "unread:count:" + userId;
    }
}
