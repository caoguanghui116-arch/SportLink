package com.mashang.registrationservice.mq;

import com.alibaba.fastjson.JSON;
import com.mashang.common.message.MessageTopic;
import com.mashang.common.message.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 报名消息生产者 —— 报名成功后异步发送确认通知。
 *
 * 业务场景：
 * - 报名高峰期（赛事开放首日）并发量大，同步发送通知会显著增加接口响应时间
 * - 将通知发送改为异步 MQ 消息，报名接口只需写 DB + 发 MQ（耗时 ~5ms）
 * - 通知由 notification-service 异步消费处理，不阻塞报名主流程
 *
 * 消息流转：
 * RegistrationController.personalEnroll() → RegistrationProducer → RocketMQ → NotificationConsumer
 */
@Slf4j
@Component
public class RegistrationProducer {

    /** RocketMQ 模板类 */
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 报名成功后发送通知消息
     *
     * @param userId        报名用户ID
     * @param eventItemName 报名项目名称
     */
    public void sendRegistrationNotification(Long userId, String eventItemName) {
        // 构建消息体
        NotificationMessage message = new NotificationMessage(
                "REGISTRATION",
                userId,
                "报名成功",
                String.format("您已成功报名项目【%s】，请密切关注赛程安排，按时参加比赛。", eventItemName)
        );

        // 构建 RocketMQ 路由目标
        String destination = MessageTopic.NOTIFICATION_TOPIC + ":" + MessageTopic.TAG_REGISTRATION;

        // 同步发送，保证消息不丢失
        rocketMQTemplate.syncSend(destination,
                MessageBuilder.withPayload(JSON.toJSONString(message)).build());

        log.info("Registration notification sent: userId={}, item={}", userId, eventItemName);
    }
}
