package com.mashang.aiservice.feign;

import com.mashang.aiservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign client for user-service.
 * Provides user profile data for RAG context.
 */
@FeignClient(
    name = "user-service",
    configuration = FeignConfig.class,
    fallbackFactory = UserServiceFeignFallbackFactory.class
)
public interface UserServiceFeign {

    /**
     * Get user information by user ID.
     */
    @GetMapping("/user/info/{userId}")
    Map<String, Object> getUserInfo(@PathVariable("userId") Long userId);
}
