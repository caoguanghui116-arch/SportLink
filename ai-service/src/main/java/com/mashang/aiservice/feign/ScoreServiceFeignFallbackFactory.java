package com.mashang.aiservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ScoreServiceFeign 的降级工厂 —— 当 score-service 不可用时提供兜底响应。
 *
 * 适用场景：
 * - score-service 宕机或正在重启
 * - 网络抖动导致 Feign 调用超时
 * - Nacos 注册中心中 score-service 实例全部下线
 *
 * 降级影响：
 * - 成绩查询、排行榜查询等 AI 问答场景将返回 "成绩数据暂不可用"
 * - AI 不会捏造成绩数据，而是诚实告知用户数据不可用
 */
@Component
public class ScoreServiceFeignFallbackFactory implements FallbackFactory<ScoreServiceFeign> {

    /**
     * 创建 score-service 的降级 Feign 客户端。
     *
     * @param cause 导致降级的原始异常（超时、连接拒绝、5xx 等）
     * @return ScoreServiceFeign 的降级实现
     */
    @Override
    public ScoreServiceFeign create(Throwable cause) {
        System.err.println("ScoreServiceFeign fallback triggered: " + cause.getMessage());
        return new ScoreServiceFeign() {
            @Override
            public Map<String, Object> getPersonalResult(Long userId) {
                return buildErrorMap("成绩服务暂不可用，无法获取个人成绩");
            }

            @Override
            public Map<String, Object> getRanking(Long meetingId) {
                return buildErrorMap("成绩服务暂不可用，无法获取排名信息");
            }

            @Override
            public Map<String, Object> getScoresByItemId(Long itemId) {
                return buildErrorMap("成绩服务暂不可用，无法获取成绩数据");
            }
        };
    }

    /**
     * 构建统一的错误响应 Map。
     *
     * @param message 中文错误提示
     * @return 包含 error 标记和 message 的 Map
     */
    private Map<String, Object> buildErrorMap(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        return error;
    }
}
