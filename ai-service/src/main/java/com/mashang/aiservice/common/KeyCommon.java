package com.mashang.aiservice.common;

import com.mashang.common.constants.CacheConstants;

/**
 * Key utility for building cache keys.
 * Simplified version - no Redis dependency needed for ai-service.
 */
public class KeyCommon {

    /** AI chat session cache key prefix */
    public static final String AI_CHAT_KEY = "ai:chat:";

    /** AI announcement cache key prefix */
    public static final String AI_ANNOUNCEMENT_KEY = "ai:announcement:";

    /** AI analysis cache key prefix */
    public static final String AI_ANALYSIS_KEY = "ai:analysis:";

    public static String buildChatKey(Long userId) {
        return AI_CHAT_KEY + userId;
    }

    public static String buildChatKey() {
        return AI_CHAT_KEY;
    }

    public static String buildAnnouncementKey(Long meetingId) {
        return AI_ANNOUNCEMENT_KEY + meetingId;
    }

    public static String buildAnnouncementKey() {
        return AI_ANNOUNCEMENT_KEY;
    }

    public static String buildAnalysisKey(Long meetingId) {
        return AI_ANALYSIS_KEY + meetingId;
    }

    public static String buildAnalysisKey() {
        return AI_ANALYSIS_KEY;
    }
}
