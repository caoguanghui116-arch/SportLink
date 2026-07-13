package com.mashang.aiservice.feign;

import com.mashang.aiservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 赛事服务远程调用接口 —— 通过 OpenFeign 调用 event-service 的 REST API。
 *
 * 调用方：ai-service（AI智能问答模块需要赛事数据来回答用户关于赛程、项目、场馆的提问）
 * 被调用方：event-service（赛事管理微服务）
 *
 * 降级策略：
 * - 配置了 fallbackFactory = EventServiceFeignFallbackFactory.class
 * - 当 event-service 不可用时返回空 Map，AI 会回复 "赛事数据暂不可用，请稍后再试"
 */
@FeignClient(
    name = "event-service",
    configuration = FeignConfig.class,
    fallbackFactory = EventServiceFeignFallbackFactory.class
)
public interface EventServiceFeign {

    /**
     * 查询比赛项目详情。
     * 调用 GET /basic/setup/project/{itemId}
     *
     * @param itemId 项目ID
     * @return 包含项目名称、分类、积分规则等信息的 Map
     */
    @GetMapping("/basic/setup/project/{itemId}")
    Map<String, Object> getItemInfo(@PathVariable("itemId") Long itemId);

    /**
     * 查询用户的赛程信息。
     * 调用 GET /schedule/user/{userId}
     *
     * @param userId 用户ID（运动员）
     * @return 包含该用户所有赛程的 Map（比赛时间、场地、项目、对手等）
     */
    @GetMapping("/schedule/user/{userId}")
    Map<String, Object> getScheduleInfo(@PathVariable("userId") Long userId);

    /**
     * 查询全部场馆列表。
     * 调用 GET /schedule/venue
     *
     * @return 包含所有场馆信息的 Map（场馆名称、容量、地址等）
     */
    @GetMapping("/schedule/venue")
    Map<String, Object> getAllVenues();

    /**
     * 查询全部运动会列表。
     * 调用 GET /basic/setup/meeting
     *
     * @return 包含所有运动会信息的 Map
     */
    @GetMapping("/basic/setup/meeting")
    Map<String, Object> getAllMeetings();

    /**
     * 按运动会ID查询赛程。
     * 调用 GET /schedule/page?meetingId={meetingId}
     *
     * @param meetingId 运动会ID
     * @return 包含该运动会所有赛程的 Map
     */
    @GetMapping("/schedule/page")
    Map<String, Object> getScheduleByMeetingId(@PathVariable("meetingId") Long meetingId);
}
