package com.mashang.aiservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * UserServiceFeign 的降级工厂 —— 当 user-service 不可用时提供兜底响应。
 *
 * 工作原理：
 * 1. OpenFeign 发起远程调用 → user-service 超时或返回 5xx
 * 2. FallbackFactory 被触发，create(Throwable) 方法被调用
 * 3. 返回一个匿名实现，每个方法都返回包含错误信息的 Map
 * 4. AI 服务收到包含 "error": true 的 Map，向用户回复友好的错误提示
 *
 * 设计要点：
 * - 每个降级方法返回的 Map 中统一包含 error=true 标记和错误消息
 * - AI 服务根据 error 标记判断数据是否可用，不可用时回复 "用户数据暂不可用"
 * - 使用 System.err 输出错误日志（生产环境应改为 log.error）
 * - 这是 Sentinel 限流降级之外的第二层保护（服务级别的容错）
 *
 * 与 Sentinel 的区别：
 * - Sentinel：流量控制层面的熔断降级（限流、慢调用、异常比例）
 * - Feign Fallback：服务调用层面的降级（目标服务完全不可用）
 */
@Component
public class UserServiceFeignFallbackFactory implements FallbackFactory<UserServiceFeign> {

    /**
     * 创建降级实现。
     * 当 user-service 不可用时，Spring 调用此方法获取兜底的 Feign 客户端实例。
     *
     * @param cause 导致降级的异常（可能是超时、连接拒绝、5xx 等）
     * @return UserServiceFeign 的降级实现，所有方法返回错误 Map
     */
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

    /**
     * 构建统一的错误响应 Map。
     * 包含 error=true 标记，方便 AI 服务调用方判断数据是否可用。
     *
     * @param message 错误提示消息（中文，可直接展示给用户）
     * @return 包含 error 标记和 message 的 Map
     */
    private Map<String, Object> buildErrorMap(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        return error;
    }
}
