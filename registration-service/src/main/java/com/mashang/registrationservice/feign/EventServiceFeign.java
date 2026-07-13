package com.mashang.registrationservice.feign;

import com.mashang.registrationservice.config.FeignConfig;
import com.mashang.common.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 赛事服务 Feign 客户端（报名服务侧） —— 报名服务调用赛事服务获取项目信息
 *
 * <p>声明式 HTTP 客户端，用于报名服务向赛事服务查询比赛项目的详细信息。
 * 用户在报名时需要了解项目规则、报名限制、比赛时间等，
 * 这些数据归属赛事服务管理，报名服务通过此客户端按需获取。
 *
 * <p>配置说明：
 * <ul>
 *   <li>{@code name = "event-service"} —— 目标微服务在注册中心的服务名</li>
 *   <li>{@code configuration = FeignConfig.class} —— 自定义 Feign 配置类，
 *       包含请求拦截器和超时配置</li>
 * </ul>
 *
 * <p>调用场景：
 * <ul>
 *   <li>报名前：校验项目是否存在、报名是否已截止、是否达到限报人数</li>
 *   <li>报名详情展示：展示项目名称、项目类型、比赛时间等</li>
 * </ul>
 *
 * <p>降级策略：
 * <ul>
 *   <li>未配置 fallback，调用失败时抛出 FeignException</li>
 *   <li>此接口对报名流程较为关键 —— 无法获取项目信息时无法校验报名资格。
 *       建议配置 Sentinel 熔断 + fallback，提供降级响应（如拒绝报名并提示"服务繁忙"）</li>
 * </ul>
 *
 * @author mashang
 * @see com.mashang.registrationservice.config.FeignConfig 对应的 Feign 配置类
 */
@FeignClient(name = "event-service", configuration = FeignConfig.class)
public interface EventServiceFeign {

    /**
     * 根据项目ID获取项目详细信息
     *
     * <p>调用赛事服务的 {@code GET /basic/setup/project/{itemId}} 接口，
     * 获取指定比赛项目的详细信息。报名服务借此校验：
     * <ul>
     *   <li>项目是否存在</li>
     *   <li>当前时间是否在报名窗口内（signStartTime ~ signEndTime）</li>
     *   <li>当前报名人数是否已到达限报人数（maxEntry）</li>
     *   <li>性别限制是否匹配（sexLimit）</li>
     * </ul>
     *
     * @param itemId 项目ID（路径参数，必填），该参数被映射到 URL 路径
     *               {@code /basic/setup/project/{itemId}} 中
     * @return 统一响应体，data 为包含项目信息的 Map 对象，
     *         典型字段包括 itemName、itemType、signStartTime、signEndTime、maxEntry、sexLimit 等
     */
    @GetMapping("/basic/setup/project/{itemId}")
    R<Map<String, Object>> getItemInfo(@PathVariable("itemId") Long itemId);
}
