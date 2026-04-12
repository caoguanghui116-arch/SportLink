package com.mashang.common.common;

import lombok.Data;
import com.mashang.common.constants.CacheConstants;

@Data
public class KeyCommon {
    /**
     * 拼接 Redis Key 方法（避免重复代码）
     */
    public String buildKey(Long id) {
        return CacheConstants.EVENT_KEY + id;
    }
}