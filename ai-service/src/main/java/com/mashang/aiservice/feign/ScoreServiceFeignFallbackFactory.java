package com.mashang.aiservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Fallback factory for ScoreServiceFeign - provides graceful degradation
 * when score-service is unavailable.
 */
@Component
public class ScoreServiceFeignFallbackFactory implements FallbackFactory<ScoreServiceFeign> {

    @Override
    public ScoreServiceFeign create(Throwable cause) {
        System.err.println("ScoreServiceFeign fallback triggered: " + cause.getMessage());
        return new ScoreServiceFeign() {
            @Override
            public Map<String, Object> getPersonalResult(Long userId, Long meetingId) {
                return buildErrorMap("成绩服务暂不可用，无法获取个人成绩");
            }

            @Override
            public Map<String, Object> getRanking(Long meetingId, Long itemId) {
                return buildErrorMap("成绩服务暂不可用，无法获取排名信息");
            }

            @Override
            public Map<String, Object> getScoresByMeetingId(Long meetingId) {
                return buildErrorMap("成绩服务暂不可用，无法获取成绩数据");
            }
        };
    }

    private Map<String, Object> buildErrorMap(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        return error;
    }
}
