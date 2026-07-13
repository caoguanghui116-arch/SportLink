package com.mashang.registrationservice.feign;

import com.mashang.registrationservice.config.FeignConfig;
import com.mashang.common.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 用户服务 Feign 客户端（报名服务侧） —— 报名服务调用用户服务获取用户信息
 *
 * <p>声明式 HTTP 客户端，用于报名服务向用户服务查询用户基本信息。
 * 报名服务在处理报名请求时，可能需要校验用户是否存在、获取用户真实姓名、
 * 检查用户是否已报名其他冲突项目等，通过此 Feign 接口向用户服务获取数据。
 *
 * <p>配置说明：
 * <ul>
 *   <li>{@code name = "user-service"} —— 目标微服务在 Nacos/Eureka 注册中心的服务名</li>
 *   <li>{@code configuration = FeignConfig.class} —— 自定义 Feign 配置类，
 *       包含请求拦截器（传递 JWT Token 以通过用户服务的认证校验）、超时配置等</li>
 * </ul>
 *
 * <p>返回值使用 {@code R<Map<String, Object>>}：
 * <ul>
 *   <li>Map 结构灵活，报名服务可按需提取需要的用户字段</li>
 *   <li>避免了报名服务引入用户服务的完整 VO 类，降低服务间编译时耦合</li>
 * </ul>
 *
 * <p>降级策略：
 * <ul>
 *   <li>未配置 fallback，调用失败时抛出 FeignException</li>
 *   <li>报名服务的核心链路（报名写入）不应被用户服务阻塞：
 *       建议在调用方捕获异常后允许报名继续（报名表中有 userId 即可，
 *       用户详情可异步补全），配合 Sentinel 熔断 + 缓存兜底</li>
 * </ul>
 *
 * @author mashang
 * @see com.mashang.registrationservice.config.FeignConfig 对应的 Feign 配置类
 */
@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserServiceFeign {

    /**
     * 根据用户ID获取用户信息
     *
     * <p>调用用户服务的 {@code GET /user/{userId}} 接口，获取指定用户的基本信息。
     * 报名服务在校验报名资格时需要确认用户是否存在且状态正常。
     *
     * @param userId 用户ID（路径参数，必填），该参数被映射到 URL 路径 {@code /user/{userId}} 中
     * @return 统一响应体，data 为包含用户信息的 Map 对象
     */
    @GetMapping("/user/{userId}")
    R<Map<String, Object>> getUserInfo(@PathVariable("userId") Long userId);
}
