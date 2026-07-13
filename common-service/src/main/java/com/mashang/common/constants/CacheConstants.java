package com.mashang.common.constants;

/**
 * Redis 缓存常量 —— 统一定义所有服务的缓存前缀和 TTL。
 *
 * 设计原则：
 * 1. 每个业务域使用独立的 Key 前缀（如 event: / score: / social:），避免不同服务间的 Key 冲突
 * 2. TTL 根据数据变化频率差异化配置，在数据一致性与缓存命中率之间取得平衡：
 *    - 写频繁的数据 TTL 短（如社交动态 5min），保证用户看到最新内容
 *    - 读多写少的数据 TTL 长（如菜单/院系 60min），最大化缓存收益
 *    - 排行榜/成绩 TTL 居中（10min），兼顾实时性与性能
 * 3. 集中管理所有 Key 前缀，方便运维排查问题
 *    - 例如：redis-cli KEYS event:*  可以查看所有赛事相关缓存
 *    - 例如：redis-cli KEYS social:* 可以查看所有社交相关缓存
 *
 * TTL 设置指南（按数据变化频率）：
 * - 5 分钟：变化频繁、对实时性要求高的数据（社交动态、评论、点赞）
 * - 10 分钟：写操作适中、可容忍短暂不一致的数据（成绩、排名）
 * - 30 分钟：写操作较少、相对稳定的数据（赛事信息、项目分类、场馆、裁判）
 * - 60 分钟：几乎不变的数据（院系、菜单、用户会话）
 *
 * Cache Aside 模式要点（所有写操作遵循）：
 * 1. 读：查缓存 → 命中返回 → 未命中查 DB → 写入缓存 → 返回
 * 2. 写：更新 DB → 删除缓存（注意：先更新 DB 再删缓存，避免并发读写导致缓存与 DB 不一致）
 */
public class CacheConstants {

    // ======================== event-service 赛事服务 ========================

    /** 赛事缓存 Key 前缀：event:（运动会信息、项目详情等） */
    public static final String EVENT_KEY = "event:";

    /** 赛事列表缓存 Key 前缀：list:event:（运动会列表、项目列表等集合数据） */
    public static final String LIST_EVENT_KEY = "list:event:";

    /** 赛事缓存 TTL：30 分钟（运动会信息变化频率低，可缓存较久） */
    public static final long EVENT_TTL = 30;

    // ======================== registration-service 报名服务 ========================

    /** 报名缓存 Key 前缀：registration:（个人报名、团体报名状态等） */
    public static final String REGISTRATION_KEY = "registration:";

    /** 报名缓存 TTL：30 分钟（报名数据需要一定实时性，但大部分时间不变） */
    public static final long REGISTRATION_TTL = 30;

    // ======================== score-service 成绩服务 ========================

    /** 成绩缓存 Key 前缀：score:（单项成绩、团体成绩等） */
    public static final String SCORE_KEY = "score:";

    /** 排行榜缓存 Key 前缀：ranking:（与成绩分开，方便独立管理 TTL） */
    public static final String RANKING_KEY = "ranking:";

    /** 成绩/排行榜缓存 TTL：10 分钟（成绩更新较频繁，需要更快刷新保证准确性） */
    public static final long SCORE_TTL = 10;

    // ======================== social-service 社交服务 ========================

    /** 社交缓存 Key 前缀：social:（动态、评论、弹幕等） */
    public static final String SOCIAL_KEY = "social:";

    /** 社交缓存 TTL：5 分钟（社交内容变化频繁，短 TTL 保证数据新鲜度） */
    public static final long SOCIAL_TTL = 5;

    // ======================== notification-service 通知服务 ========================

    /** 通知缓存 Key 前缀：notification:（消息列表、公告等） */
    public static final String NOTIFICATION_KEY = "notification:";

    /** 通知缓存 TTL：30 分钟（通知读多写少，可缓存较久） */
    public static final long NOTIFICATION_TTL = 30;

    // ======================== user-service 用户服务 ========================

    /** 用户缓存 Key 前缀：user:（用户信息、登录状态等） */
    public static final String USER_KEY = "user:";

    /** 用户会话缓存 TTL：60 分钟（登录状态，过期后需重新登录获取新 Token） */
    public static final long USER_TTL = 60;
}
