package com.mashang.eventservice.common;

import com.mashang.common.constants.CacheConstants;

/**
 * 缓存 Key 构造工具类 —— 集中管理 event-service 的 Redis Key 命名规范。
 *
 * 设计原则：
 * 1. 所有缓存 Key 统一前缀，方便运维排查（执行 KEYS list:event:* 可查看所有赛事列表缓存）
 * 2. 用不同 Key 区分不同业务场景，避免 Key 冲突和数据覆盖
 * 3. 命名格式：{业务域}:{实体类型}:{标识符}
 *
 * Key 命名规范示例：
 * - list:event:all           → 运动会全量列表
 * - schedule:meeting:1       → 运动会 ID=1 的赛程数据
 * - event:meeting:1          → 运动会 ID=1 的详细信息
 *
 * 使用方式：
 * <pre>
 *   String cacheKey = KeyCommon.buildScheduleKey(meetingId);
 *   Object cached = redisTemplate.opsForValue().get(cacheKey);
 * </pre>
 */
public class KeyCommon {

    /**
     * 构建赛事项目全量列表缓存 Key。
     *
     * 格式：list:event:all
     * 存储内容：List&lt;EventItemVo&gt;（全量项目列表，含项目名称、分类、积分规则等信息）
     * 使用场景：赛程编排时选择项目、报表统计时展示全部项目
     * TTL：30 分钟（CacheConstants.EVENT_TTL）
     *
     * @return 缓存 Key 字符串
     */
    public static String buildKey() {
        return CacheConstants.LIST_EVENT_KEY + "all";
    }

    /**
     * 构建赛程数据缓存 Key（按运动会ID）。
     *
     * 格式：schedule:meeting:{meetingId}
     * 存储内容：Page&lt;ScheduleVo&gt;（分页的赛程数据，包含比赛时间、场地、项目、参赛者）
     * 使用场景：观众端查看赛程表、运动员查看自己的比赛安排
     * TTL：30 分钟
     *
     * @param meetingId 运动会ID
     * @return 缓存 Key 字符串，如 "schedule:meeting:1"
     */
    public static String buildScheduleKey(Long meetingId) {
        return "schedule:meeting:" + meetingId;
    }

    /**
     * 构建运动会信息缓存 Key（按运动会ID）。
     *
     * 格式：event:meeting:{meetingId}
     * 存储内容：SportsMeeting 实体 或 List&lt;SportsMeeting&gt;
     * 使用场景：查询特定运动会的详细信息（名称、时间、场馆、状态等）
     * TTL：30 分钟
     *
     * @param meetingId 运动会ID
     * @return 缓存 Key 字符串，如 "event:meeting:1"
     */
    public static String buildMeetingKey(Long meetingId) {
        return CacheConstants.EVENT_KEY + "meeting:" + meetingId;
    }
}
