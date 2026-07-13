package com.mashang.notificationservice.feign;

import com.mashang.notificationservice.config.FeignConfig;
import com.mashang.common.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign 客户端（通知服务侧） —— 通知服务调用用户服务获取用户信息
 *
 * <p>声明式 HTTP 客户端，用于通知服务向用户服务查询用户基本信息。
 * 通知服务在发送通知前可能需要校验目标用户是否存在，或在通知内容中嵌入用户名等信息，
 * 通过此 Feign 客户端向用户服务查询。
 *
 * <p>配置说明：
 * <ul>
 *   <li>{@code name = "user-service"} —— 目标微服务在注册中心的服务名，
 *       Feign 结合负载均衡组件将请求路由到可用的用户服务实例</li>
 *   <li>{@code configuration = FeignConfig.class} —— 自定义 Feign 配置类，
 *       通常配置有请求拦截器、编解码器、超时等</li>
 * </ul>
 *
 * <p>注意：与社交服务中的 UserServiceFeign 不同，此接口的返回值类型为 {@code R<?>}
 * 而非 {@code R<Map<String, Object>>}，说明通知服务对用户信息的反序列化更加宽松，
 * 可能只关心调用是否成功（HTTP 200），而不关心具体返回字段。
 *
 * <p>降级策略：
 * <ul>
 *   <li>调用失败时抛出 FeignException，由调用方捕获处理</li>
 *   <li>通知服务的核心链路不应强依赖用户服务 —— 即使无法获取用户详情，
 *       通知本身仍应成功发送（user-service 不可用不应阻塞通知下发）</li>
 *   <li>建议添加 Sentinel 熔断兜底，调用失败时仍然发送通知但跳过用户信息丰富步骤</li>
 * </ul>
 *
 * @author mashang
 * @see com.mashang.notificationservice.config.FeignConfig 对应的 Feign 配置类
 */
@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserServiceFeign {

    /**
     * 根据用户ID获取用户信息
     *
     * <p>调用用户服务的 {@code GET /user/{userId}} 接口，获取指定用户的基本信息。
     * 返回值为 {@code R<?>} 通配符类型，说明通知服务不关心返回值的具体结构，
     * 仅需确认目标用户存在且调用成功即可。
     *
     * @param userId 用户ID（路径参数，必填），该参数被映射到 URL 路径 {@code /user/{userId}} 中
     * @return 统一响应体，data 类型为通配符（不关心具体结构）
     */
    @GetMapping("/user/{userId}")
    R<?> getUserInfo(@PathVariable("userId") Long userId);
}
