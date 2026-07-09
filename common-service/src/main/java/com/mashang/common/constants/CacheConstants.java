package com.mashang.common.constants;

/**
 * Redis 缓存常量 —— 统一定义所有服务的缓存前缀和 TTL。
 *
 * 设计原则：
 * 1. 每个业务域独立 Key 前缀（如 event: / score:），避免 Key 冲突
 * 2. TTL 差异化配置，平衡数据一致性与缓存命中率：
 *    - 写频繁的数据 TTL 短（如社交 5min）
 *    - 读多写少的数据 TTL 长（如赛事 30min）
 *    - 用户会话 TTL 最长（如登录状态 60min）
 * 3. 集中管理，方便运维排查（KEYS event:* 查看所有赛事缓存）
 *
 * TTL 设置参考：
 * - 5 分钟：写操作频繁、对实时性要求高的数据（如社交点赞数）
 * - 10 分钟：写操作适中、可容忍短暂不一致的数据（如排行榜）
 * - 30 分钟：写操作少、相对稳定的数据（如赛事信息、赛程）
 * - 60 分钟：几乎不变的数据（如用户登录会话）
 */
public class CacheConstants {

    // ==================== event-service ====================

    /** 赛事缓存 Key 前缀：event: */
    public static final String EVENT_KEY = "event:";

    /** 赛事列表缓存 Key 前缀：list:event: */
    public static final String LIST_EVENT_KEY = "list:event:";

    /** 赛事缓存 TTL：30 分钟（赛事信息变化频率低，可缓存较久） */
    public static final long EVENT_TTL = 30;

    // ==================== registration-service ====================

    /** 报名缓存 Key 前缀：registration: */
    public static final String REGISTRATION_KEY = "registration:";

    /** 报名缓存 TTL：30 分钟（报名数据需要一定实时性，但不能太短） */
    public static final long REGISTRATION_TTL = 30;

    // ==================== score-service ====================

    /** 成绩缓存 Key 前缀：score: */
    public static final String SCORE_KEY = "score:";

    /** 排行榜缓存 Key 前缀：ranking:（与成绩分开，方便独立管理 TTL） */
    public static final String RANKING_KEY = "ranking:";

    /** 成绩/排行榜缓存 TTL：10 分钟（成绩更新相对频繁，需要更快刷新） */
    public static final long SCORE_TTL = 10;

    // ==================== social-service ====================

    /** 社交缓存 Key 前缀：social: */
    public static final String SOCIAL_KEY = "social:";

    /** 社交缓存 TTL：5 分钟（社交动态/评论变化频繁，短 TTL 保证数据新鲜度） */
    public static final long SOCIAL_TTL = 5;

    // ==================== notification-service ====================

    /** 通知缓存 Key 前缀：notification: */
    public static final String NOTIFICATION_KEY = "notification:";

    /** 通知缓存 TTL：30 分钟（通知读多写少，可缓存较久） */
    public static final long NOTIFICATION_TTL = 30;

    // ==================== user-service ====================

    /** 用户缓存 Key 前缀：user: */
    public static final String USER_KEY = "user:";

    /** 用户会话缓存 TTL：60 分钟（登录状态，过期需重新登录） */
    public static final long USER_TTL = 60;
}
