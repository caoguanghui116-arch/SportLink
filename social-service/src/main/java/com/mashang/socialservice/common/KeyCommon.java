package com.mashang.socialservice.common;

import lombok.Data;
import com.mashang.common.constants.CacheConstants;

@Data
public class KeyCommon {
    /**
     * 拼接 Redis Key 方法（避免重复代码）
     */
    public static String buildKey(Long id) {
        return CacheConstants.SOCIAL_KEY + id;
    }

    public static String buildKey() {
        return CacheConstants.SOCIAL_KEY;
    }
}
