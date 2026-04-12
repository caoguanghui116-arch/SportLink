package com.mashang.aiservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Fallback factory for UserServiceFeign - provides graceful degradation
 * when user-service is unavailable.
 */
@Component
public class UserServiceFeignFallbackFactory implements FallbackFactory<UserServiceFeign> {

    @Override
    public UserServiceFeign create(Throwable cause) {
        System.err.println("UserServiceFeign fallback triggered: " + cause.getMessage());
        return new UserServiceFeign() {
            @Override
            public Map<String, Object> getUserInfo(Long userId) {
                return buildErrorMap("用户服务暂不可用，无法获取用户信息");
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
