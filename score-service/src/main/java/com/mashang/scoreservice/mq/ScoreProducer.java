package com.mashang.scoreservice.mq;

import com.alibaba.fastjson.JSON;
import com.mashang.common.message.MessageTopic;
import com.mashang.common.message.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 成绩发布消息生产者 —— 成绩录入后异步发送通知。
 *
 * 为什么用 RocketMQ 异步发送：
 * 1. 成绩录入是核心链路，需要快速响应；通知发送是附加功能，不应拖慢主流程
 * 2. 通知发送失败（如 notification-service 宕机）不应影响成绩录入成功
 * 3. MQ 自带重试机制，保证通知消息最终一定会被消费（最终一致性）
 *
 * 消息流转：
 * ScoreController.personalEntry() → ScoreProducer → RocketMQ → NotificationConsumer
 */
@Slf4j
@Component
public class ScoreProducer {

    /** RocketMQ 模板类，封装了消息发送、事务消息、顺序消息等操作 */
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 成绩发布后发送通知消息（同步发送，保证消息不丢）
     *
     * @param userId       接收通知的运动员ID
     * @param eventItemName  比赛项目名称
     * @param score          最终成绩
     */
    public void sendScorePublishNotification(Long userId, String eventItemName, String score) {
        // 构建消息体
        NotificationMessage message = new NotificationMessage(
                "SCORE_PUBLISH",
                userId,
                "比赛成绩已发布",
                String.format("您在项目【%s】中的成绩【%s】已正式发布，可前往成绩中心查看详情。",
                        eventItemName, score)
        );

        // 构建 RocketMQ 路由目标： Topic:Tag
        String destination = MessageTopic.NOTIFICATION_TOPIC + ":" + MessageTopic.TAG_SCORE_PUBLISH;

        // syncSend：同步发送，等待 Broker 确认后才返回，保证投递可靠性
        rocketMQTemplate.syncSend(destination,
                MessageBuilder.withPayload(JSON.toJSONString(message)).build());

        log.info("Score publish notification sent: userId={}, item={}", userId, eventItemName);
    }
}
