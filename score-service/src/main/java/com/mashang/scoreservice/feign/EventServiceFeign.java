package com.mashang.scoreservice.feign;

import com.mashang.common.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 赛事服务 Feign 客户端（成绩服务侧） —— 成绩服务调用赛事服务获取赛程/项目信息
 *
 * <p>声明式 HTTP 客户端，用于成绩服务向赛事服务查询赛程项目列表。
 * 成绩服务在录入成绩时需要确认项目存在、获取比赛时间等，
 * 通过此 Feign 客户端向赛事服务查询。
 *
 * <p>配置说明：
 * <ul>
 *   <li>{@code name = "event-service"} —— 目标微服务在注册中心的服务名，
 *       Feign 结合 Ribbon/LoadBalancer 实现客户端负载均衡，自动选择可用实例</li>
 *   <li>{@code configuration = FeignConfig.class} —— 自定义 Feign 配置类，
 *       通常包含请求拦截器（JWT Token 传递）、超时配置（建议 3 秒以上）等</li>
 * </ul>
 *
 * <p>注意：此接口调用的是 {@code GET /schedule/item}，
 * 与报名服务中的 EventServiceFeign 不同（报名服务调用 {@code /basic/setup/project/{itemId}}），
 * 本接口获取的是包含赛程信息的项目列表，更适合成绩录入场景。
 *
 * <p>降级策略：
 * <ul>
 *   <li>未配置 fallback，调用失败时抛出 FeignException</li>
 *   <li>成绩服务的成绩写入是核心链路 —— 如果此接口调用失败（无法校验项目存在性），
 *       建议降级为允许写入但标记为"待确认"状态，待赛事服务恢复后异步校验补全。
 *       或配置 Sentinel 熔断，在赛事服务不可用时返回空列表并提示用户</li>
 * </ul>
 *
 * @author mashan
 */
@FeignClient(name = "event-service")
public interface EventServiceFeign {

    /**
     * 获取全部赛程项目信息
     *
     * <p>调用赛事服务的 {@code GET /schedule/item} 接口，获取所有带赛程信息的项目列表。
     * 成绩服务在以下场景调用：
     * <ul>
     *   <li>裁判在选择"为哪个项目录入成绩"时，前端下拉框数据来源</li>
     *   <li>校验项目是否存在、是否已排赛程（无赛程的项目不应录入成绩）</li>
     * </ul>
     *
     * @return 统一响应体，data 为 Object 类型（具体结构由调用方按需解析），
     *         实际包含赛程关联的项目列表，每个项目有 itemId、itemName、scheduleId、gameTime 等字段
     */
    @GetMapping("/schedule/item")
    R<Object> allItem();
}
