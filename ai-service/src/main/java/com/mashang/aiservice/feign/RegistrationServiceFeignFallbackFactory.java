package com.mashang.aiservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * RegistrationServiceFeign 的降级工厂 —— 当 registration-service 不可用时提供兜底响应。
 *
 * 降级策略：
 * - 报名信息查询失败时，AI 会回复 "报名数据暂不可用"
 * - 不影响其他功能（赛事查询、成绩查询等仍可正常工作）
 * - 这是一种优雅降级，保证了 AI 服务的部分可用性
 *
 * 注意：
 * - 降级是最后一道防线，不应该在正常情况下触发
 * - 如果降级频繁触发，需要排查 registration-service 的健康状况
 * - 生产环境应将 System.err 替换为日志框架（如 log.error）
 */
@Component
public class RegistrationServiceFeignFallbackFactory implements FallbackFactory<RegistrationServiceFeign> {

    /**
     * 创建 registration-service 的降级 Feign 客户端。
     *
     * @param cause 导致降级的原始异常
     * @return RegistrationServiceFeign 的降级实现
     */
    @Override
    public RegistrationServiceFeign create(Throwable cause) {
        System.err.println("RegistrationServiceFeign fallback triggered: " + cause.getMessage());
        return new RegistrationServiceFeign() {
            @Override
            public Map<String, Object> getRegistrationInfo(Long userId) {
                return buildErrorMap("报名服务暂不可用，无法获取报名信息");
            }

            @Override
            public Map<String, Object> countByItemId(Long itemId) {
                return buildErrorMap("报名服务暂不可用，无法获取报名统计");
            }

            @Override
            public Map<String, Object> getRegistrationsByMeetingId(Long meetingId) {
                return buildErrorMap("报名服务暂不可用，无法获取报名列表");
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
