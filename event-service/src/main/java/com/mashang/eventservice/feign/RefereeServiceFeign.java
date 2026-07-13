package com.mashang.eventservice.feign;

import com.mashang.eventservice.config.FeignConfig;
import com.mashang.common.common.R;
import com.mashang.eventservice.domain.vo.RefereeVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 裁判服务 Feign 客户端 —— 赛事服务调用用户服务获取裁判信息
 *
 * <p>声明式 HTTP 客户端，通过 Spring Cloud OpenFeign 实现跨服务调用。
 * 赛事服务在编排赛程时需要获取裁判列表，通过此接口调用用户服务（裁判数据所属服务）
 * 的 {@code GET /referee} 接口拉取全部裁判信息。
 *
 * <p>配置说明：
 * <ul>
 *   <li>{@code name = "user-service"} —— 目标微服务在注册中心（Nacos/Eureka）中的服务名，
 *       Feign 通过服务名做负载均衡调用</li>
 *   <li>{@code configuration = FeignConfig.class} —— 自定义 Feign 配置类，
 *       通常包含请求拦截器（添加认证 Token）、编解码器、超时设置等</li>
 * </ul>
 *
 * <p>降级策略：
 * <ul>
 *   <li>未配置 fallback/fallbackFactory，默认情况下调用失败会抛出 FeignException</li>
 *   <li>如需自定义降级逻辑，可通过实现此接口的 Fallback 类或 FallbackFactory 类，
 *       并在 {@code @FeignClient} 注解中添加 {@code fallback = XxxFallback.class} 属性</li>
 *   <li>建议配合 Sentinel 熔断降级，在调用方（赛事服务）做兜底处理</li>
 * </ul>
 *
 * @author mashang
 * @see com.mashang.eventservice.config.FeignConfig 对应的 Feign 配置类
 */
@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface RefereeServiceFeign {

    /**
     * 获取全部裁判信息
     *
     * <p>调用用户服务的 {@code GET /referee} 接口，返回所有裁判列表。
     * 裁判数据实际存储在 user-service 的数据库中（裁判是用户的一种角色），
     * 赛事服务不存储裁判数据，通过此接口按需获取。
     *
     * <p>调用场景：管理员创建/编辑赛程时需要选择裁判，前端下拉框数据来源。
     *
     * @return 统一响应体，data 为 {@link RefereeVo} 列表，
     *         每个 RefereeVo 包含裁判ID、姓名、联系方式等基本信息
     */
    @GetMapping("/referee")
    R<List<RefereeVo>> allReferee();
}
