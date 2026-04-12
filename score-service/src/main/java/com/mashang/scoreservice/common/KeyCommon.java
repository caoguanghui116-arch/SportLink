package com.mashang.scoreservice.common;

import lombok.Data;
import com.mashang.common.constants.CacheConstants;

@Data
public class KeyCommon {

    /**
     * 拼接 Redis Key 方法（成绩key）
     */
    public static String buildScoreKey(Long id) {
        return CacheConstants.SCORE_KEY + id;
    }

    public static String buildScoreKey() {
        return CacheConstants.SCORE_KEY;
    }

    /**
     * 拼接 Redis Key 方法（排行榜key）
     */
    public static String buildRankingKey(Long meetingId) {
        return CacheConstants.RANKING_KEY + meetingId;
    }

    public static String buildRankingKey() {
        return CacheConstants.RANKING_KEY;
    }
}
