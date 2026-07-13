package com.mashang.aiservice.feign;

import com.mashang.aiservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 报名服务远程调用接口 —— 通过 OpenFeign 调用 registration-service 的 REST API。
 *
 * 调用方：ai-service（AI智能问答模块需要报名数据来回答用户关于报名情况的提问）
 * 被调用方：registration-service（报名管理微服务）
 *
 * 降级策略：
 * - 配置了 fallbackFactory = RegistrationServiceFeignFallbackFactory.class
 * - 当 registration-service 不可用时返回空 Map，AI 会回复 "报名数据暂不可用"
 */
@FeignClient(
    name = "registration-service",
    configuration = FeignConfig.class,
    fallbackFactory = RegistrationServiceFeignFallbackFactory.class
)
public interface RegistrationServiceFeign {

    /**
     * 查询用户的报名信息。
     * 调用 GET /registration/personal/user/{userId}
     *
     * @param userId 用户ID
     * @return 包含该用户所有报名记录的 Map
     */
    @GetMapping("/registration/personal/user/{userId}")
    Map<String, Object> getRegistrationInfo(@PathVariable("userId") Long userId);

    /**
     * 统计某项目的报名人数。
     * 调用 GET /registration/personal/item/{itemId}/count
     *
     * @param itemId 比赛项目ID
     * @return 包含该项目报名人数统计的 Map
     */
    @GetMapping("/registration/personal/item/{itemId}/count")
    Map<String, Object> countByItemId(@PathVariable("itemId") Long itemId);

    /**
     * 查询某运动会的团体报名列表。
     * 调用 GET /registration/team/meeting/{meetingId}
     *
     * @param meetingId 运动会ID
     * @return 包含该运动会所有团体报名记录的 Map
     */
    @GetMapping("/registration/team/meeting/{meetingId}")
    Map<String, Object> getRegistrationsByMeetingId(@PathVariable("meetingId") Long meetingId);
}
