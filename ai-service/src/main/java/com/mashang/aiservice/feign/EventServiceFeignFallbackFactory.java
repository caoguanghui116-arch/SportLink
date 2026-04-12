package com.mashang.aiservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Fallback factory for EventServiceFeign - provides graceful degradation
 * when event-service is unavailable.
 */
@Component
public class EventServiceFeignFallbackFactory implements FallbackFactory<EventServiceFeign> {

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
            public Map<String, Object> getVenueInfo(Long venueId) {
                return buildErrorMap("赛事服务暂不可用，无法获取场地信息");
            }

            @Override
            public Map<String, Object> getMeetingInfo(Long meetingId) {
                return buildErrorMap("赛事服务暂不可用，无法获取赛事信息");
            }

            @Override
            public Map<String, Object> getScheduleByMeetingId(Long meetingId) {
                return buildErrorMap("赛事服务暂不可用，无法获取赛程信息");
            }
        };
    }

    private Map<String, Object> buildErrorMap(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        return error;
    }
}
