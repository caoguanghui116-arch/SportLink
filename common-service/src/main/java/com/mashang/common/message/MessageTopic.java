package com.mashang.common.message;

/**
 * RocketMQ Topic 与 Tag 常量定义 —— 统一消息路由规则。
 *
 * RocketMQ 的消息路由结构：Topic（一级分类） + Tag（二级分类）
 *  - Topic 类比"快递公司"，Tag 类比"包裹类型"
 *  - Consumer 可按 Topic + Tag 组合订阅，实现精准消费
 *
 * 使用示例：
 *   destination = "sportlink-notification:score-publish"
 *   → Topic = sportlink-notification, Tag = score-publish
 */
public final class MessageTopic {

    private MessageTopic() {
        // 工具类禁止实例化
    }

    // ==================== Topic 定义 ====================

    /** 通知消息 Topic：所有类型的通知走同一个 Topic，通过 Tag 区分类型 */
    public static final String NOTIFICATION_TOPIC = "sportlink-notification";

    // ==================== Tag 定义 ====================

    /** 成绩发布通知 Tag —— score-service 成绩录入后触发 */
    public static final String TAG_SCORE_PUBLISH = "score-publish";

    /** 报名成功通知 Tag —— registration-service 报名成功后触发 */
    public static final String TAG_REGISTRATION = "registration";

    /** 公告发布通知 Tag —— 管理员发布公告后触发 */
    public static final String TAG_ANNOUNCEMENT = "announcement";
}
