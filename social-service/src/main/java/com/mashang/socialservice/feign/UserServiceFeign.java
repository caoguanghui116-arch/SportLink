package com.mashang.socialservice.feign;

import com.mashang.common.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 用户服务 Feign 客户端（社交服务侧） —— 社交服务调用用户服务获取用户信息
 *
 * <p>声明式 HTTP 客户端，用于社交服务向用户服务查询用户基本信息。
 * 社交服务中的动态、评论、弹幕等模块需要展示用户的头像、昵称等信息，
 * 但社交服务不冗余存储用户资料，而是通过此 Feign 客户端实时向用户服务查询。
 *
 * <p>配置说明：
 * <ul>
 *   <li>{@code name = "user-service"} —— 目标微服务在注册中心的服务名，
 *       Feign 结合 Ribbon/LoadBalancer 实现客户端负载均衡</li>
 *   <li>{@code configuration = FeignConfig.class} —— 自定义 Feign 配置类，
 *       通常包含请求拦截器（JWT Token 传递）、日志级别、超时等配置</li>
 * </ul>
 *
 * <p>降级策略：
 * <ul>
 *   <li>未显式配置 fallback/fallbackFactory，调用失败时抛出 FeignException</li>
 *   <li>建议配合 Sentinel 熔断，或在调用方缓存用户信息以减少对 user-service 的依赖，
 *       避免用户服务不可用时社交服务的动态列表也无法加载</li>
 * </ul>
 *
 * @author mashang
 */
@FeignClient(name = "user-service")
public interface UserServiceFeign {

    /**
     * 根据用户ID获取用户信息
     *
     * <p>调用用户服务的 {@code GET /user/{userId}} 接口，获取指定用户的基本信息。
     * 社交服务在展示动态作者、评论者、弹幕发送者的头像和昵称时调用此方法。
     *
     * <p>返回值为 {@code R<Map<String, Object>>}，使用 Map 而非强类型 VO 的原因是：
     * <ul>
     *   <li>社交服务可能只需要用户的少量字段（如头像、昵称），
     *       不同场景需要的字段不同，Map 更加灵活</li>
     *   <li>避免社交服务引入用户服务的完整 VO 依赖，降低服务间耦合</li>
     * </ul>
     *
     * @param userId 用户ID（路径参数，必填），该参数被映射到 URL 路径 {@code /user/{userId}} 中
     * @return 统一响应体，data 为包含用户信息的 Map 对象，
     *         典型字段包括 id、username、realName、avatar、phone 等
     */
    @GetMapping("/user/{userId}")
    R<Map<String, Object>> getUserInfo(@PathVariable("userId") Long userId);
}
