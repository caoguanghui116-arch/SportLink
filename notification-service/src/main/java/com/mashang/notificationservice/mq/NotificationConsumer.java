package com.mashang.notificationservice.mq;

import com.alibaba.fastjson.JSON;
import com.mashang.common.message.MessageTopic;
import com.mashang.common.message.NotificationMessage;
import com.mashang.notificationservice.domain.entity.Notification;
import com.mashang.notificationservice.service.INotificationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知消息消费者 —— 从 RocketMQ 消费通知消息并持久化到数据库。
 *
 * 消费模式：
 * - 集群消费（默认）：同一条消息只会被 group 内的一个实例消费，不会重复
 * - selectorExpression："*" 表示消费该 Topic 下的所有 Tag
 * - Consumer 宕机不影响 Producer，消息堆积在 MQ 中等待消费恢复
 *
 * 为什么用异步消费：
 * - 通知写入 DB 和 Redis 后，用户需要通过"通知列表"接口才能看到
 * - 写入操作相对耗时（DB + Redis），但不影响用户当前操作（报名/查成绩）
 * - MQ 削峰：报名高峰期，消息排队消费，不会击垮 notification-service
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = MessageTopic.NOTIFICATION_TOPIC,        // 消费的 Topic
        consumerGroup = "notification-consumer-group",  // 消费者组（组内负载均衡）
        selectorExpression = "*"                         // 消费所有 Tag（* = 通配）
)
public class NotificationConsumer implements RocketMQListener<String> {

    /**
     * 消息类型到数据库 type 字段的映射
     * 数据库 type 为 Long 类型：1=成绩发布, 2=报名成功, 3=公告发布
     */
    private static final Map<String, Long> TYPE_MAPPING = new HashMap<>();

    static {
        TYPE_MAPPING.put("SCORE_PUBLISH", 1L);
        TYPE_MAPPING.put("REGISTRATION", 2L);
        TYPE_MAPPING.put("ANNOUNCEMENT", 3L);
    }

    @Resource
    private INotificationService notificationService;

    /**
     * 消息消费入口 —— RocketMQ 推送消息后调用此方法
     *
     * @param messageBody JSON 格式的通知消息字符串
     */
    @Override
    public void onMessage(String messageBody) {
        log.info("Received notification message: {}", messageBody);
        try {
            // 1. JSON 反序列化
            NotificationMessage message = JSON.parseObject(messageBody, NotificationMessage.class);

            // 2. 构建 Notification 实体
            Notification notification = new Notification();
            notification.setUserId(message.getUserId());
            notification.setTitle(message.getTitle());
            notification.setContent(message.getContent());
            notification.setType(TYPE_MAPPING.getOrDefault(message.getType(), 0L));
            notification.setIsRead(0L);  // 默认未读

            // 3. 持久化到数据库（MyBatis-Plus IService.save 内部处理 insert）
            notificationService.save(notification);

            log.info("Notification persisted: userId={}, type={}",
                    message.getUserId(), message.getType());
        } catch (Exception e) {
            // 消费异常只记录日志，不抛异常 → 消息返回 MQ 会触发重试机制
            log.error("Failed to process notification message: {}", messageBody, e);
        }
    }
}
