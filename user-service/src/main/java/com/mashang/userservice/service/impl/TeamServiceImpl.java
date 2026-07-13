package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.Team;
import com.mashang.userservice.mapper.TeamMapper;
import com.mashang.userservice.service.ITeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 团体服务实现
 *
 * <h3>核心职责：</h3>
 * <ul>
 *   <li>团体的增删改查基本操作</li>
 *   <li>团体名称唯一性校验</li>
 *   <li>按院系筛选团体</li>
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
 *   <li>全部团体列表：team:all（TTL 30 分钟）</li>
 *   <li>按院系查询：team:dept:{deptId}（TTL 30 分钟）</li>
 * </ul>
 *
 * <h3>设计思路：</h3>
 * <ul>
 *   <li>继承 MyBatis-Plus 的 ServiceImpl 获得基础 CRUD 能力</li>
 *   <li>团体通过 deptId 字段关联所属院系</li>
 *   <li>添加团队时进行名称唯一性校验，防止同一院系下创建重名团体</li>
 *   <li>写操作后清除缓存而非更新缓存，避免并发写入导致的数据不一致</li>
 * </ul>
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements ITeamService {

    // ======================== 缓存 Key 常量 ========================

    /** 全部团体列表的 Redis 缓存 Key */
    private static final String TEAM_ALL_KEY = "team:all";

    /** 按院系查询团体的 Redis 缓存 Key 前缀，完整格式为 team:dept:{deptId} */
    private static final String TEAM_DEPT_PREFIX = "team:dept:";

    // ======================== 依赖注入 ========================

    /** 团体数据访问层 */
    @Autowired
    private TeamMapper teamMapper;

    /** Redis 模板，用于缓存操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ======================== 写操作（含唯一性校验与缓存清除） ========================

    /**
     * 添加团体
     *
     * <h3>执行流程：</h3>
     * <ol>
     *   <li><b>唯一性校验：</b>查询数据库中是否已存在同名的团体名称，防止重复创建</li>
     *   <li><b>写入数据库：</b>执行 INSERT 操作</li>
     *   <li><b>清除缓存：</b>删除所有团体相关缓存（Cache Aside 写模式）</li>
     * </ol>
     *
     * @param team 团体实体，需包含 teamName、deptId 等字段
     * @return 影响行数（>0 表示添加成功）
     * @throws RuntimeException 当团体名称已存在时抛出
     */
    @Override
    public int add(Team team) {
        // 团体名称唯一性校验：查询同名记录
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getTeamName, team.getTeamName());
        if (teamMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("团队名称已存在");
        }

        // 写入数据库
        int result = teamMapper.insert(team);

        // 写入成功后删除缓存（Cache Aside 写模式：淘汰缓存而非更新缓存）
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    /**
     * 修改团体信息
     *
     * 更新数据库后清除缓存，保证下次查询获取到最新数据
     *
     * @param team 团体实体，id 字段必填
     * @return 影响行数（>0 表示修改成功）
     */
    @Override
    public int update(Team team) {
        int result = teamMapper.updateById(team);
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    /**
     * 删除团体
     *
     * 物理删除数据库记录后清除缓存
     *
     * @param teamId 团体ID
     * @return 影响行数（>0 表示删除成功）
     */
    @Override
    public int delete(Long teamId) {
        int result = teamMapper.deleteById(teamId);
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    // ======================== 读操作（Cache Aside 读模式） ========================

    /**
     * 获取所有团体列表 —— Cache Aside 读模式
     *
     * <h3>执行流程：</h3>
     * <ol>
     *   <li>从 Redis 读取 Key 为 team:all 的缓存数据</li>
     *   <li>缓存命中 → 直接返回（不查数据库）</li>
     *   <li>缓存未命中 → 查询数据库全表 → 将结果写入 Redis（TTL 30 分钟）→ 返回结果</li>
     * </ol>
     *
     * @return 团体实体列表
     */
    @Override
    public List<Team> listAll() {
        // Step 1: 读缓存
        Object cached = redisTemplate.opsForValue().get(TEAM_ALL_KEY);
        if (cached != null) {
            // 缓存命中
            return (List<Team>) cached;
        }

        // Step 2: 查数据库
        List<Team> list = teamMapper.selectList(null);

        // Step 3: 回写缓存，TTL 30 分钟（仅当结果非空时写入）
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(TEAM_ALL_KEY, list, 30, TimeUnit.MINUTES);
        }
        return list;
    }

    /**
     * 按院系ID查询团体列表 —— Cache Aside 读模式
     *
     * <h3>使用场景：</h3>
     * 在赛程编排或报名管理页面中，按院系筛选该院系下的所有参赛团体
     *
     * <h3>缓存 Key 格式：</h3>
     * team:dept:{deptId}，例如 team:dept:3 表示院系ID为3的团体列表
     *
     * @param deptId 院系ID
     * @return 该院系下的团体实体列表
     */
    @Override
    public List<Team> listByDeptId(Long deptId) {
        // 构建缓存 Key
        String key = TEAM_DEPT_PREFIX + deptId;

        // Step 1: 读缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            // 缓存命中
            return (List<Team>) cached;
        }

        // Step 2: 查数据库 —— WHERE dept_id = ?
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getDeptId, deptId);
        List<Team> list = teamMapper.selectList(wrapper);

        // Step 3: 回写缓存，TTL 30 分钟
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(key, list, 30, TimeUnit.MINUTES);
        }
        return list;
    }

    // ======================== 缓存管理 ========================

    /**
     * 清除所有团体相关缓存
     *
     * <h3>清除策略：</h3>
     * <ol>
     *   <li>删除 team:all 键（全部团体列表缓存）</li>
     *   <li>使用 keys 命令匹配 team:dept:* 并批量删除（所有按院系查询的缓存）</li>
     * </ol>
     *
     * <h3>为什么用 keys 通配符删除？</h3>
     * 团体可能关联多个院系，而写操作（增/改/删）时无法确定具体影响了哪些院系的数据，
     * 因此采用批量通配符删除所有 team:dept:* 键作为安全策略。
     * 生产环境中若 Redis 键数量极大，可改用 SCAN 命令配合批量删除来避免阻塞。
     */
    private void evictCache() {
        // 删除全部列表缓存
        redisTemplate.delete(TEAM_ALL_KEY);
        // 批量删除所有按院系查询的缓存（通配符匹配 team:dept:*）
        redisTemplate.delete(redisTemplate.keys(TEAM_DEPT_PREFIX + "*"));
    }
}
