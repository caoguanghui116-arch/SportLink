package com.mashang.eventservice.common;

import com.mashang.common.constants.CacheConstants;

/**
 * 缓存 Key 构造工具类 —— 集中管理 event-service 的 Redis Key 命名规范。
 *
 * 设计原则：
 * - 所有缓存 Key 统一前缀，方便运维排查（KEYS list:event:*）
 * - 用不同 Key 区分不同业务场景，避免 Key 冲突
 * - 命名格式：{业务域}:{实体类型}:{ID}
 */
public class KeyCommon {

    /**
     * 赛事项目列表缓存 Key
     * 格式：list:event:all
     * 存储内容：List<EventItemVo>（全量项目列表）
     * TTL：30 分钟
     */
    public static String buildKey() {
        return CacheConstants.LIST_EVENT_KEY + "all";
    }

    /**
     * 赛程分页缓存 Key（按运动会ID）
     * 格式：schedule:meeting:1
     * 存储内容：Page<ScheduleVo>
     * TTL：30 分钟
     */
    public static String buildScheduleKey(Long meetingId) {
        return "schedule:meeting:" + meetingId;
    }

    /**
     * 运动会信息缓存 Key
     * 格式：event:meeting:1
     * 存储内容：SportsMeeting / List<SportsMeeting>
     * TTL：30 分钟
     */
    public static String buildMeetingKey(Long meetingId) {
        return CacheConstants.EVENT_KEY + "meeting:" + meetingId;
    }
}
