package com.mashang.aiservice.utils;

import com.mashang.common.common.R;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enterprise-grade unified API response structure (simplified version).
 *
 * @param <T> data type returned
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** 0 = success, non-zero = error */
    private int code;

    /** Error or success description */
    private String message;

    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(-1, message, null);
    }

    protected R toR(int rows) {
        return rows > 0 ? R.ok() : R.fail();
    }
}
