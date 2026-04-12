package com.mashang.registrationservice.common;

import com.mashang.common.constants.CacheConstants;

public class KeyCommon {

    public static String buildKey(Long id) {
        return CacheConstants.REGISTRATION_KEY + id;
    }

    public static String buildKey() {
        return CacheConstants.REGISTRATION_KEY;
    }
}
