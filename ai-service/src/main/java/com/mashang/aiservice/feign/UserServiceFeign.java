package com.mashang.aiservice.feign;

import com.mashang.aiservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 用户服务远程调用接口 —— 通过 OpenFeign 调用 user-service 的 REST API。
 *
 * 调用方：ai-service（AI智能问答模块需要获取用户信息来个性化回复）
 * 被调用方：user-service（用户微服务）
 *
 * 降级策略：
 * - 配置了 fallbackFactory = UserServiceFeignFallbackFactory.class
 * - 当 user-service 不可用时，返回降级数据（默认空 Map），避免 AI 服务雪崩
 *
 * 配置说明：
 * - FeignConfig.class 中配置了请求拦截器，自动携带认证 Token 到下游服务
 */
@FeignClient(
    name = "user-service",                                    // Nacos 注册中心的服务名
    configuration = FeignConfig.class,                         // Feign 配置（编解码器、拦截器等）
    fallbackFactory = UserServiceFeignFallbackFactory.class    // 降级工厂（服务不可用时触发）
)
public interface UserServiceFeign {

    /**
     * 查询用户详细信息。
     * 调用 user-service 的 GET /user/{userId} 接口。
     *
     * @param userId 用户ID
     * @return 包含用户信息的 Map（username, realName, phone, roleId 等字段）
     */
    @GetMapping("/user/{userId}")
    Map<String, Object> getUserInfo(@PathVariable("userId") Long userId);
}
