package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.eventservice.domain.entity.EventCategory;
import com.mashang.eventservice.mapper.EventCategoryMapper;
import com.mashang.eventservice.service.IEventCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 项目分类服务实现类
 * <p>
 * 核心职责：负责运动会项目分类（EventCategory）的增删改查，采用 Cache-Aside 缓存策略。
 * <p>
 * 业务背景：运动会中的比赛项目需要按类别分组管理（如田径、球类、水上项目等），
 * 每个分类关联到一个运动会（meetingId），分类之间按 sortOrder 排序。
 * <p>
 * 缓存策略（Cache-Aside 模式）：
 * <ol>
 *   <li><b>查询</b>：先查 Redis → 命中返回 → 未命中查 DB → 回写 Redis（TTL 30 分钟）</li>
 *   <li><b>写入</b>：先写 DB → 成功后删除对应 meetingId 维度的 Redis 缓存</li>
 * </ol>
 * <p>
 * 缓存 Key 设计：event:category:meeting:{meetingId}
 * 以 meetingId 为维度隔离缓存，不同运动会的分类数据互不影响，
 * 更新某个运动会的分类时只清除对应的缓存，不会影响其他运动会的缓存命中率。
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，自动获得基础 CRUD 能力。
 *
 * @author mashang
 */
@Service
public class EventCategoryServiceImpl extends ServiceImpl<EventCategoryMapper, EventCategory> implements IEventCategoryService {

    /**
     * 分类缓存 Key 前缀
     * 完整 Key 格式：event:category:meeting:{meetingId}
     * 示例：event:category:meeting:1001
     */
    private static final String CATEGORY_MEETING_PREFIX = "event:category:meeting:";

    /** 项目分类 Mapper —— 用于自定义数据库操作 */
    @Autowired
    private EventCategoryMapper eventCategoryMapper;

    /** Redis 模板 —— 用于 Cache-Aside 缓存读写操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 新增项目分类
     * <p>
     * 执行流程：
     * <ol>
     *   <li><b>唯一性校验</b>：同一运动会下，分类名称不可重复</li>
     *   <li><b>写入 DB</b>：执行 INSERT 操作</li>
     *   <li><b>删除缓存</b>：清除该运动会对应的分类缓存（key = event:category:meeting:{meetingId}）</li>
     * </ol>
     * <p>
     * 唯一性校验说明：使用 meetingId + categoryName 组合条件查询，
     * 确保同一运动会内分类名称唯一，但不同运动会可以有同名分类。
     *
     * @param category 分类实体对象，需包含 categoryName（分类名称）、meetingId（所属运动会）、sortOrder（排序序号）
     * @return 受影响的行数，大于 0 表示新增成功
     * @throws RuntimeException 当该运动会下已存在同名分类时抛出
     */
    @Override
    public int addCategory(EventCategory category) {
        // 唯一性校验：同一运动会下分类名称不可重复
        LambdaQueryWrapper<EventCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventCategory::getCategoryName, category.getCategoryName())
                .eq(EventCategory::getMeetingId, category.getMeetingId());
        if (eventCategoryMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("该分类名称已存在");
        }

        // 执行 INSERT
        int result = eventCategoryMapper.insert(category);

        // Cache-Aside：写 DB 成功后删除该运动会维度的缓存
        if (result > 0) {
            redisTemplate.delete(CATEGORY_MEETING_PREFIX + category.getMeetingId());
        }
        return result;
    }

    /**
     * 根据运动会 ID 查询分类列表
     * <p>
     * 采用完整的 Cache-Aside 模式：
     * <ol>
     *   <li><b>步骤1 - 查缓存</b>：从 Redis 读取 key = event:category:meeting:{meetingId}</li>
     *   <li><b>步骤2 - 缓存命中</b>：直接返回反序列化后的分类列表</li>
     *   <li><b>步骤3 - 缓存未命中</b>：从 DB 查询该运动会下所有有效分类</li>
     *   <li><b>步骤4 - 回写缓存</b>：将结果写入 Redis，TTL 30 分钟</li>
     * </ol>
     * <p>
     * 查询条件：
     * <ul>
     *   <li>meetingId 精确匹配</li>
     *   <li>delFlag = 0（过滤逻辑删除的记录）</li>
     * </ul>
     * 排序依据：按 sortOrder 升序排列（sort_order 越小越靠前），
     * 支持前端拖拽排序后刷新列表显示。
     *
     * @param meetingId 运动会 ID
     * @return 该运动会下的分类列表，按 sortOrder 升序排列
     */
    @Override
    public List<EventCategory> listByMeetingId(Long meetingId) {
        // 构建缓存 Key：event:category:meeting:{meetingId}
        String key = CATEGORY_MEETING_PREFIX + meetingId;

        // ---- 第1步：查 Redis 缓存 ----
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (List<EventCategory>) cached; // 缓存命中，直接返回
        }

        // ---- 第2步：缓存未命中 → 查数据库 ----
        LambdaQueryWrapper<EventCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventCategory::getMeetingId, meetingId)        // 按运动会 ID 过滤
                .eq(EventCategory::getDelFlag, 0)                  // 过滤已逻辑删除的记录
                .orderByAsc(EventCategory::getSortOrder);          // 按排序序号升序排列

        List<EventCategory> list = eventCategoryMapper.selectList(wrapper);

        // ---- 第3步：回写 Redis 缓存 ----
        // TTL 30 分钟：分类数据变动频率低，较长 TTL 可提高缓存命中率
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(key, list, 30, TimeUnit.MINUTES);
        }
        return list;
    }
}
