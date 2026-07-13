package com.mashang.aiservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * EventServiceFeign 的降级工厂 —— 当 event-service 不可用时提供兜底响应。
 *
 * 工作原理：
 * 1. OpenFeign 发起远程调用 → event-service 超时或返回 5xx
 * 2. FallbackFactory.create(Throwable) 被调用
 * 3. 返回匿名实现，所有方法返回包含 "error":true 的 Map
 * 4. AI 服务收到降级响应后，向用户回复 "赛事数据暂不可用，请稍后再试"
 *
 * 注意：
 * - 赛事数据对 AI 问答质量影响较大（涉及赛程、场馆、项目等核心问答场景）
 * - 降级后 AI 应明确告知用户数据不可用，而非给出错误的答案
 */
@Component
public class EventServiceFeignFallbackFactory implements FallbackFactory<EventServiceFeign> {

    /**
     * 创建 event-service 的降级 Feign 客户端。
     *
     * @param cause 导致降级的原始异常
     * @return EventServiceFeign 的降级实现
     */
    @Override
    public EventServiceFeign create(Throwable cause) {
        System.err.println("EventServiceFeign fallback triggered: " + cause.getMessage());
        return new EventServiceFeign() {
            @Override
            public Map<String, Object> getItemInfo(Long itemId) {
                return buildErrorMap("赛事服务暂不可用，无法获取项目信息");
            }

            @Override
            public Map<String, Object> getScheduleInfo(Long userId) {
                return buildErrorMap("赛事服务暂不可用，无法获取赛程信息");
            }

            @Override
            public Map<String, Object> getAllVenues() {
                return buildErrorMap("赛事服务暂不可用，无法获取场地信息");
            }

            @Override
            public Map<String, Object> getAllMeetings() {
                return buildErrorMap("赛事服务暂不可用，无法获取赛事信息");
            }

            @Override
            public Map<String, Object> getScheduleByMeetingId(Long meetingId) {
                return buildErrorMap("赛事服务暂不可用，无法获取赛程信息");
            }
        };
    }

    /**
     * 构建统一的错误响应 Map。
     *
     * @param message 中文错误提示
     * @return 包含 error 标记和 message 的 Map
     */
    private Map<String, Object> buildErrorMap(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        return error;
    }
}
