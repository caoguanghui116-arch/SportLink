package com.mashang.aiservice.feign;

import com.mashang.aiservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 成绩服务远程调用接口 —— 通过 OpenFeign 调用 score-service 的 REST API。
 *
 * 调用方：ai-service（AI智能问答模块需要成绩/排名数据来回答用户关于比赛结果的提问）
 * 被调用方：score-service（成绩管理微服务）
 *
 * 降级策略：
 * - 配置了 fallbackFactory = ScoreServiceFeignFallbackFactory.class
 * - 当 score-service 不可用时返回空 Map，AI 会回复 "成绩数据暂不可用"
 */
@FeignClient(
    name = "score-service",
    configuration = FeignConfig.class,
    fallbackFactory = ScoreServiceFeignFallbackFactory.class
)
public interface ScoreServiceFeign {

    /**
     * 查询用户的个人成绩。
     * 调用 GET /score/personal/user/{userId}
     *
     * @param userId 用户ID（运动员）
     * @return 包含该用户所有参赛项目成绩的 Map
     */
    @GetMapping("/score/personal/user/{userId}")
    Map<String, Object> getPersonalResult(@PathVariable("userId") Long userId);

    /**
     * 查询运动会排行榜。
     * 调用 GET /score/ranking/{meetingId}
     *
     * @param meetingId 运动会ID
     * @return 包含该运动会排行榜数据的 Map（院系排名、团体排名等）
     */
    @GetMapping("/score/ranking/{meetingId}")
    Map<String, Object> getRanking(@PathVariable("meetingId") Long meetingId);

    /**
     * 查询某项目的所有成绩。
     * 调用 GET /score/personal/item/{itemId}
     *
     * @param itemId 比赛项目ID
     * @return 包含该项目所有参赛者成绩的 Map
     */
    @GetMapping("/score/personal/item/{itemId}")
    Map<String, Object> getScoresByItemId(@PathVariable("itemId") Long itemId);
}
