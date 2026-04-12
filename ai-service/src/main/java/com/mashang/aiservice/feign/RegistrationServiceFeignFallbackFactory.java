package com.mashang.aiservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Fallback factory for RegistrationServiceFeign - provides graceful degradation
 * when registration-service is unavailable.
 */
@Component
public class RegistrationServiceFeignFallbackFactory implements FallbackFactory<RegistrationServiceFeign> {

    @Override
    public RegistrationServiceFeign create(Throwable cause) {
        System.err.println("RegistrationServiceFeign fallback triggered: " + cause.getMessage());
        return new RegistrationServiceFeign() {
            @Override
            public Map<String, Object> getRegistrationInfo(Long userId, Long meetingId) {
                return buildErrorMap("报名服务暂不可用，无法获取报名信息");
            }

            @Override
            public Map<String, Object> countByItemId(Long itemId) {
                return buildErrorMap("报名服务暂不可用，无法获取报名统计");
            }

            @Override
            public Map<String, Object> getRegistrationsByMeetingId(Long meetingId) {
                return buildErrorMap("报名服务暂不可用，无法获取报名列表");
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
