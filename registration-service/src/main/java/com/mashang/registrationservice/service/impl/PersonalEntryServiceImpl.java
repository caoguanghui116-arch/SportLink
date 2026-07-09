package com.mashang.registrationservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.common.constants.CacheConstants;
import com.mashang.registrationservice.common.KeyCommon;
import com.mashang.registrationservice.domain.entity.PersonalEntry;
import com.mashang.registrationservice.domain.query.create.PersonalEntryQuery;
import com.mashang.registrationservice.domain.vo.PersonalEntryVo;
import com.mashang.registrationservice.feign.EventServiceFeign;
import com.mashang.registrationservice.mapper.PersonalEntryMapper;
import com.mashang.registrationservice.mapping.PersonalEntryMapping;
import com.mashang.registrationservice.service.IPersonalEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 个人报名服务 —— 含 Cache-Aside 缓存策略。
 *
 * 缓存场景分析：
 * - 用户报名列表（按 userId）：查看"我报了哪些项目"（高频读，低频写）
 * - 项目报名计数（按 itemId）：查看"这个项目有多少人报名"（高频读，写频繁）
 * - 写操作（报名/取消）：不仅更新 DB，还需要清除相关缓存
 *
 * 注意：报名高峰期写操作频繁，缓存 TTL 不宜过长（30分钟），避免数据不一致窗口过大
 */
@Service
public class PersonalEntryServiceImpl extends ServiceImpl<PersonalEntryMapper, PersonalEntry> implements IPersonalEntryService {

    @Autowired
    private PersonalEntryMapper personalEntryMapper;

    @Autowired
    private EventServiceFeign eventServiceFeign;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 个人报名 —— 写 DB 后清除相关缓存。
     *
     * 缓存失效策略：
     * - 清除项目报名计数缓存（countByItemId 用）
     * - 清除用户报名列表缓存（listByUserId 用）
     * - 采用"删除缓存"而非"更新缓存"：避免并发写导致的缓存与 DB 不一致
     */
    @Override
    public int enroll(PersonalEntryQuery query) {
        // 防重复报名：同一项目 + 同一用户 + 同一运动会 + 状态有效
        LambdaQueryWrapper<PersonalEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PersonalEntry::getUserId, query.getUserId())
                .eq(PersonalEntry::getItemId, query.getItemId())
                .eq(PersonalEntry::getMeetingId, query.getMeetingId())
                .eq(PersonalEntry::getStatus, 1);  // 1=已报名

        if (personalEntryMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("您已报名该项目，请勿重复报名");
        }

        PersonalEntry entry = PersonalEntryMapping.INSTANCE.toEntity(query);
        entry.setStatus(1L);  // 状态：1=已报名，2=已取消
        int rows = personalEntryMapper.insert(entry);

        // 报名成功 → 清除缓存（下次查询自动重新加载）
        if (rows > 0) {
            redisTemplate.delete(KeyCommon.buildKey(query.getItemId()));         // 项目计数缓存
            redisTemplate.delete(KeyCommon.buildKey(query.getUserId()));         // 用户列表缓存
        }
        return rows;
    }

    /**
     * 取消报名 —— 写 DB 后清除相关缓存。
     */
    @Override
    public int cancel(Long entryId, Long userId) {
        LambdaQueryWrapper<PersonalEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PersonalEntry::getEntryId, entryId)
                .eq(PersonalEntry::getUserId, userId);

        PersonalEntry entry = personalEntryMapper.selectOne(wrapper);
        if (entry == null) {
            throw new RuntimeException("报名记录不存在");
        }

        entry.setStatus(2L);  // 2=已取消（逻辑删除）
        int rows = personalEntryMapper.updateById(entry);

        if (rows > 0) {
            redisTemplate.delete(KeyCommon.buildKey(entry.getItemId()));
            redisTemplate.delete(KeyCommon.buildKey(userId));
        }
        return rows;
    }

    /**
     * 查询用户报名列表 —— Cache-Aside 模式。
     *
     * 缓存 Key：registration:{userId}
     * 缓存 TTL：30 分钟
     */
    @Override
    public List<PersonalEntryVo> listByUserId(Long userId) {
        String cacheKey = KeyCommon.buildKey(userId);

        // 1. 查缓存
        List<PersonalEntryVo> cacheList = (List<PersonalEntryVo>) redisTemplate.opsForValue().get(cacheKey);
        if (cacheList != null) {
            return cacheList;
        }

        // 2. 缓存未命中 → 查 DB
        List<PersonalEntryVo> result = personalEntryMapper.selectByUserId(userId);

        // 3. 写缓存（含穿透防护）
        if (result == null || result.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, "NULL", 2, TimeUnit.MINUTES);  // 短 TTL 防穿透
        } else {
            redisTemplate.opsForValue().set(cacheKey, result,
                    CacheConstants.REGISTRATION_TTL, TimeUnit.MINUTES);
        }
        return result;
    }

    @Override
    public PersonalEntryVo detail(Long entryId) {
        return personalEntryMapper.selectDetailById(entryId);
    }

    /**
     * 查询项目报名人数 —— Cache-Aside 模式。
     *
     * 缓存 Key：registration:{itemId}:count
     * 缓存 TTL：30 分钟
     *
     * 优化说明：
     * - 之前只读缓存、不写缓存，导致缓存形同虚设（每次都穿透到 DB）
     * - 修复后：读缓存 → 未命中 → 查 DB → 写缓存 → 下次直接命中
     */
    @Override
    public int countByItemId(Long itemId) {
        String cacheKey = KeyCommon.buildKey(itemId) + ":count";

        // 1. 查缓存（缓存的是数字字符串，如 "156"）
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Integer.parseInt(cached.toString());
        }

        // 2. 缓存未命中 → 查 DB
        int count = personalEntryMapper.countByItemId(itemId);

        // 3. 写缓存
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(count),
                CacheConstants.REGISTRATION_TTL, TimeUnit.MINUTES);
        return count;
    }
}
