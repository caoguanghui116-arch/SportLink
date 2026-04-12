package com.mashang.eventservice.utils;

import com.mashang.eventservice.domain.entity.R;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业级统一 API 响应结构（简化版）
 * T 为返回的数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;       // 0 表示成功，非 0 表示错误
    private String message; // 错误或成功描述
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(-1, message, null);
    }

    protected R toR(int rows)
    {
        return rows > 0 ? R.ok() : R.fail();
    }
}
