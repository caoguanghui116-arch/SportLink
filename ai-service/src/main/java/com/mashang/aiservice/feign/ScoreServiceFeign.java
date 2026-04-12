package com.mashang.aiservice.feign;

import com.mashang.aiservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign client for score-service.
 * Provides score and ranking data for RAG context.
 */
@FeignClient(
    name = "score-service",
    configuration = FeignConfig.class,
    fallbackFactory = ScoreServiceFeignFallbackFactory.class
)
public interface ScoreServiceFeign {

    /**
     * Get personal results for a user in a meeting.
     */
    @GetMapping("/score/personal")
    Map<String, Object> getPersonalResult(
            @RequestParam("userId") Long userId,
            @RequestParam("meetingId") Long meetingId);

    /**
     * Get ranking data for a meeting, optionally filtered by item.
     */
    @GetMapping("/score/ranking")
    Map<String, Object> getRanking(
            @RequestParam("meetingId") Long meetingId,
            @RequestParam(value = "itemId", required = false) Long itemId);

    /**
     * Get all scores for a specific meeting.
     */
    @GetMapping("/score/meeting/{meetingId}")
    Map<String, Object> getScoresByMeetingId(@PathVariable("meetingId") Long meetingId);
}
