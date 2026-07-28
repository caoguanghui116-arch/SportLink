package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.Department;
import com.mashang.userservice.domain.query.create.DepartmentCreateQuerry;
import com.mashang.userservice.domain.query.update.DepartmentUpdateQuerry;
import com.mashang.userservice.mapper.DepartmentMapper;
import com.mashang.userservice.mapping.DepartmentMapping;
import com.mashang.userservice.service.IDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 院系服务实现
 *
 * <h3>核心职责：</h3>
 * <ul>
 *   <li>院系的增删改查基本操作</li>
 *   <li>院系名称唯一性校验</li>
 *   <li>按父级ID查询子院系（支持树形层级结构）</li>
 *   <li>Redis 缓存管理（Cache Aside 模式）</li>
 * </ul>
 *
 * <h3>Redis 缓存策略 —— Cache Aside 模式：</h3>
 * <ol>
 *   <li><b>读操作（listAll / listByParentId）：</b>
 *       先查 Redis 缓存 → 命中则直接返回 → 未命中则查数据库 → 将结果写入 Redis 并设置 TTL</li>
 *   <li><b>写操作（add / update / delete）：</b>
 *       先写数据库 → 成功后立即删除所有相关 Redis 缓存键 → 下次读取时自动重建缓存</li>
 * </ol>
 *
 * <h3>缓存 Key 约定：</h3>
 * <ul>
 *   <li>全部院系列表：department:all（TTL 60 分钟）</li>
 *   <li>按父级ID查询：department:parent:{parentId}（TTL 60 分钟）</li>
 * </ul>
 *
 * <h3>排序规则：</h3>
 * 所有院系查询结果按 sort 字段升序排列，用于前端树形组件的有序展示
 *
 * <h3>设计思路：</h3>
 * <ul>
 *   <li>继承 MyBatis-Plus 的 ServiceImpl 获得基础 CRUD 能力</li>
 *   <li>添加院系时进行名称唯一性校验，避免创建重名院系</li>
 *   <li>树形结构通过 parentId 字段实现：topLevel 院系的 parentId 为 0</li>
 * </ul>
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {

    // ======================== 缓存 Key 常量 ========================

    /** 全部院系列表的 Redis 缓存 Key */
    private static final String DEPT_ALL_KEY = "department:all";

    /** 按父级ID查询子院系的 Redis 缓存 Key 前缀，完整格式为 department:parent:{parentId} */
    private static final String DEPT_PARENT_PREFIX = "department:parent:";

    // ======================== 依赖注入 ========================

    /** 院系数据访问层 */
    @Autowired
    private DepartmentMapper departmentMapper;

    /** Redis 模板，用于缓存操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ======================== 写操作（含唯一性校验与缓存清除） ========================

    /**
     * 添加院系
     *
     * <h3>执行流程：</h3>
     * <ol>
     *   <li><b>唯一性校验：</b>查询数据库中是否已存在同名的院系，防止重复创建</li>
     *   <li><b>写入数据库：</b>执行 INSERT 操作</li>
     *   <li><b>清除缓存：</b>删除所有院系相关缓存（Cache Aside 写模式）</li>
     * </ol>
     *
     * @param department 院系实体，需包含 deptName、parentId、sort 等字段
     * @return 影响行数（>0 表示添加成功）
     * @throws RuntimeException 当院系名称已存在时抛出
     */
    @Override
    public int add(DepartmentCreateQuerry department) {
        // 院系名称唯一性校验
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getDeptName, department.getDeptName());
        if (departmentMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("院系名称已存在");
        }

        // 写入数据库
        int result = departmentMapper.insert(DepartmentMapping.INSTANCE.department(department));

        // 写入成功后删除缓存（Cache Aside 写模式：淘汰缓存而非更新缓存）
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    /**
     * 修改院系信息
     *
     * 更新数据库后清除缓存，保证下次查询获取到最新数据
     *
     * @param department 院系实体，id 字段必填
     * @return 影响行数（>0 表示修改成功）
     */
    @Override
    public int update(DepartmentUpdateQuerry department) {
        LambdaUpdateWrapper<Department> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Department::getDeptId,department.getDeptId())
                .set(Department::getParentId,department.getDeptId())
                .set(Department::getDeptName,department.getDeptName())
                .set(Department::getSort,department.getSort());

        int result = departmentMapper.update(null,updateWrapper);
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    /**
     * 删除院系
     *
     * 物理删除数据库记录后清除缓存
     *
     * @param deptId 院系ID
     * @return 影响行数（>0 表示删除成功）
     */
    @Override
    public int delete(Long deptId) {
        int result = departmentMapper.deleteById(deptId);
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    // ======================== 读操作（Cache Aside 读模式） ========================

    /**
     * 获取所有院系列表 —— Cache Aside 读模式
     *
     * <h3>排序规则：</h3>
     * 按 sort 字段升序排列，确保前端渲染的院系树形组件保持正确的展示顺序
     *
     * <h3>执行流程：</h3>
     * <ol>
     *   <li>读 Redis 缓存 department:all</li>
     *   <li>缓存命中 → 直接返回</li>
     *   <li>缓存未命中 → 查数据库（ORDER BY sort ASC）→ 回写 Redis（TTL 60 分钟）</li>
     * </ol>
     *
     * @return 按 sort 升序排列的院系实体列表
     */
    @Override
    public List<Department> listAll() {
        // Step 1: 读缓存
        Object cached = redisTemplate.opsForValue().get(DEPT_ALL_KEY);
        if (cached != null) {
            return (List<Department>) cached;
        }

        // Step 2: 查数据库 —— 按 sort 字段升序排列
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Department::getSort);
        List<Department> list = departmentMapper.selectList(wrapper);

        // Step 3: 回写缓存，TTL 60 分钟
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(DEPT_ALL_KEY, list, 60, TimeUnit.MINUTES);
        }
        return list;
    }

    /**
     * 按父级ID查询子院系列表 —— Cache Aside 读模式
     *
     * <h3>使用场景：</h3>
     * 前端院系树形组件点击某个节点展开时，调用此方法获取其直属子院系
     *
     * <h3>排序规则：</h3>
     * 按 sort 字段升序排列，保证同级院系按预设顺序展示
     *
     * @param parentId 父级院系ID（topLevel 院系的 parentId 为 0）
     * @return 指定父级下的子院系列表，按 sort 升序排列
     */
    @Override
    public List<Department> listByParentId(Long parentId) {
        // 构建缓存 Key，格式: department:parent:{parentId}
        String key = DEPT_PARENT_PREFIX + parentId;

        // Step 1: 读缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (List<Department>) cached;
        }

        // Step 2: 查数据库
        // WHERE parent_id = ? ORDER BY sort ASC
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getParentId, parentId).orderByAsc(Department::getSort);
        List<Department> list = departmentMapper.selectList(wrapper);

        // Step 3: 回写缓存，TTL 60 分钟
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(key, list, 60, TimeUnit.MINUTES);
        }
        return list;
    }

    // ======================== 缓存管理 ========================

    /**
     * 清除所有院系相关缓存
     *
     * <h3>清除策略：</h3>
     * <ol>
     *   <li>删除 department:all 键（全部院系列表缓存）</li>
     *   <li>使用 keys 命令匹配 department:parent:* 并批量删除（所有按父级查询的缓存）</li>
     * </ol>
     *
     * <h3>为什么删除而非更新？</h3>
     * 采用 Cache Aside 模式的"删除缓存"策略而非"更新缓存"，原因：
     * <ul>
     *   <li>更新缓存可能引入并发写入导致的数据不一致</li>
     *   <li>删除缓存后，下次读取会从数据库重新加载最新数据，保证一致性</li>
     *   <li>院系数据的读写比例远大于写，删除缓存的性能开销可忽略</li>
     * </ul>
     */
    private void evictCache() {
        // 删除全部院系列表缓存
        redisTemplate.delete(DEPT_ALL_KEY);
        // 批量删除所有按父级查询的缓存（通配符匹配 department:parent:*）
        redisTemplate.delete(redisTemplate.keys(DEPT_PARENT_PREFIX + "*"));
    }
}
