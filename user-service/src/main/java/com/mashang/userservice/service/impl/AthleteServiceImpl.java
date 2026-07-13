package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.Athlete;
import com.mashang.userservice.mapper.AthleteMapper;
import com.mashang.userservice.service.IAthleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 运动员服务实现
 *
 * <h3>核心职责：</h3>
 * <ul>
 *   <li>运动员的增删改查基本操作</li>
 *   <li>按院系筛选运动员</li>
 *   <li>Redis 缓存管理（Cache Aside 模式）</li>
 * </ul>
 *
 * <h3>Redis 缓存策略 —— Cache Aside 模式：</h3>
 * <ol>
 *   <li><b>读操作（listAll / listByDeptId）：</b>
 *       先查 Redis 缓存 → 命中则直接返回 → 未命中则查数据库 → 将结果写入 Redis 并设置 TTL</li>
 *   <li><b>写操作（add / update / delete）：</b>
 *       先写数据库 → 成功后立即删除所有相关 Redis 缓存键 → 下次读取时自动重建缓存</li>
 * </ol>
 *
 * <h3>缓存 Key 约定：</h3>
 * <ul>
 *   <li>全部运动员列表：athlete:all（TTL 30 分钟）</li>
 *   <li>按院系查询：athlete:dept:{deptId}（TTL 30 分钟）</li>
 * </ul>
 *
 * <h3>设计思路：</h3>
 * <ul>
 *   <li>继承 MyBatis-Plus 的 ServiceImpl 获得基础 CRUD 能力</li>
 *   <li>写操作先更新数据库，成功后删除缓存（而非更新缓存），避免并发写导致缓存与DB不一致</li>
 *   <li>evictCache() 使用 keys 命令通配符删除所有 athlete:dept:* 键，
 *       因为不知道哪些院系受到影响，采用安全的全量清除策略</li>
 * </ul>
 */
@Service
public class AthleteServiceImpl extends ServiceImpl<AthleteMapper, Athlete> implements IAthleteService {

    // ======================== 缓存 Key 常量 ========================

    /** 全部运动员列表的 Redis 缓存 Key */
    private static final String ATHLETE_ALL_KEY = "athlete:all";

    /** 按院系查询运动员的 Redis 缓存 Key 前缀，完整格式为 athlete:dept:{deptId} */
    private static final String ATHLETE_DEPT_PREFIX = "athlete:dept:";

    // ======================== 依赖注入 ========================

    /** 运动员数据访问层 */
    @Autowired
    private AthleteMapper athleteMapper;

    /** Redis 模板，用于缓存操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ======================== 写操作（含缓存清除） ========================

    /**
     * 添加运动员
     *
     * 写入数据库成功后立即清除所有运动员相关缓存，
     * 确保下次查询能获取到包含新记录的最新数据
     *
     * @param athlete 运动员实体
     * @return 影响行数（>0 表示添加成功）
     */
    @Override
    public int add(Athlete athlete) {
        // 先写入数据库
        int result = athleteMapper.insert(athlete);
        // 写入成功后删除缓存（Cache Aside 写模式：淘汰缓存而非更新缓存）
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    /**
     * 修改运动员信息
     *
     * 更新数据库后清除缓存，保证数据一致性
     *
     * @param athlete 运动员实体，id 字段必填
     * @return 影响行数（>0 表示修改成功）
     */
    @Override
    public int update(Athlete athlete) {
        // 先更新数据库
        int result = athleteMapper.updateById(athlete);
        // 更新成功后删除缓存
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    /**
     * 删除运动员
     *
     * 物理删除数据库记录后清除缓存
     *
     * @param athleteId 运动员ID
     * @return 影响行数（>0 表示删除成功）
     */
    @Override
    public int delete(Long athleteId) {
        // 先删除数据库记录
        int result = athleteMapper.deleteById(athleteId);
        // 删除成功后清除缓存
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    // ======================== 读操作（Cache Aside 读模式） ========================

    /**
     * 获取所有运动员列表 —— Cache Aside 读模式
     *
     * <h3>执行流程：</h3>
     * <ol>
     *   <li>从 Redis 读取 Key 为 athlete:all 的缓存数据</li>
     *   <li>缓存命中 → 直接返回（不查数据库）</li>
     *   <li>缓存未命中 → 查询数据库全表 → 将结果写入 Redis（TTL 30 分钟）→ 返回结果</li>
     * </ol>
     *
     * @return 运动员实体列表
     */
    @Override
    public List<Athlete> listAll() {
        // Step 1: 读缓存
        Object cached = redisTemplate.opsForValue().get(ATHLETE_ALL_KEY);
        if (cached != null) {
            // 缓存命中
            return (List<Athlete>) cached;
        }

        // Step 2: 查数据库
        List<Athlete> list = athleteMapper.selectList(null);

        // Step 3: 回写缓存（仅当结果非空时写入，避免缓存空集合）
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(ATHLETE_ALL_KEY, list, 30, TimeUnit.MINUTES);
        }
        return list;
    }

    /**
     * 按院系ID查询运动员列表 —— Cache Aside 读模式
     *
     * <h3>缓存 Key 格式：</h3>
     * athlete:dept:{deptId}，例如 athlete:dept:5 表示院系ID为5的运动员列表
     *
     * @param deptId 院系ID
     * @return 该院系下的运动员实体列表
     */
    @Override
    public List<Athlete> listByDeptId(Long deptId) {
        // 构建缓存 Key
        String key = ATHLETE_DEPT_PREFIX + deptId;

        // Step 1: 读缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            // 缓存命中
            return (List<Athlete>) cached;
        }

        // Step 2: 查数据库 —— 构造 WHERE dept_id = ? 条件
        LambdaQueryWrapper<Athlete> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Athlete::getDeptId, deptId);
        List<Athlete> list = athleteMapper.selectList(wrapper);

        // Step 3: 回写缓存，TTL 30 分钟
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(key, list, 30, TimeUnit.MINUTES);
        }
        return list;
    }

    // ======================== 缓存管理 ========================

    /**
     * 清除所有运动员相关缓存
     *
     * <h3>清除策略：</h3>
     * <ol>
     *   <li>删除 athlete:all 键（全部运动员列表缓存）</li>
     *   <li>使用 keys 命令匹配 athlete:dept:* 并批量删除（所有按院系查询的缓存）</li>
     * </ol>
     *
     * <h3>为什么用 keys 而不是逐个删除？</h3>
     * 写操作（增/改/删）时无法预知影响了哪些院系的数据，
     * 因此采用 keys 通配符匹配所有 athlete:dept:* 键进行全量清除。
     * 在高并发场景下，keys 命令可能阻塞 Redis，可考虑改用 SCAN 命令优化，
     * 或使用 Set 结构维护所有 athlete:dept:* 的 Key 列表。
     */
    private void evictCache() {
        // 删除全部列表缓存
        redisTemplate.delete(ATHLETE_ALL_KEY);
        // 批量删除所有按院系查询的缓存（通配符匹配 athlete:dept:*）
        redisTemplate.delete(redisTemplate.keys(ATHLETE_DEPT_PREFIX + "*"));
    }
}
