package com.mashang.common.message;

import java.io.Serializable;

/**
 * RocketMQ 通知消息体 —— 跨服务异步通信的标准化数据结构。
 *
 * 为什么用 RocketMQ 而不是直接 Feign 同步调用：
 * - 通知发送属于"非核心链路"，不应拖慢主流程（如成绩录入）
 * - 解耦：Producer 不关心谁来消费、消费是否成功
 * - 削峰：报名高峰时通知写入 MQ 排队，Consumer 按自身节奏消费
 *
 * 使用方式：
 * 1. Producer 构建此对象 → JSON 序列化 → 发送到 RocketMQ Topic
 * 2. Consumer 接收 JSON 字符串 → 反序列化为此对象 → 持久化到数据库
 *
 * 注意：必须实现 Serializable，因为 RocketMQ 默认使用 Java 序列化
 */
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息类型：
     *  SCORE_PUBLISH — 成绩发布通知
     *  REGISTRATION  — 报名成功通知
     *  ANNOUNCEMENT  — 公告发布通知
     *  对应 MessageTopic 中的 TAG_* 常量
     */
    private String type;

    /** 接收通知的目标用户ID */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 关联业务ID（如赛事ID、成绩ID），用于"点击通知跳转到详情页" */
    private Long businessId;

    /** 消息产生时间戳（毫秒），用于消费端判断消息延迟 */
    private Long timestamp;

    // ==================== 构造方法 ====================

    public NotificationMessage() {
    }

    /**
     * 快捷构造方法
     * @param type    消息类型
     * @param userId  目标用户ID
     * @param title   通知标题
     * @param content 通知内容
     */
    public NotificationMessage(String type, Long userId, String title, String content) {
        this.type = type;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.timestamp = System.currentTimeMillis();  // 自动填充时间戳
    }

    // ==================== Getter / Setter ====================

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "type='" + type + '\'' +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                '}';
    }
}
