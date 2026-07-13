package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.SysMenu;
import com.mashang.userservice.mapper.SysMenuMapper;
import com.mashang.userservice.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统菜单服务实现
 *
 * <h3>核心职责：</h3>
 * <ul>
 *   <li>菜单的增删改查基本操作</li>
 *   <li>菜单名称唯一性校验</li>
 *   <li>按角色查询菜单权限（RBAC 权限模型）</li>
 *   <li>菜单树形查询（按 parentId + sort 排序，前端直接渲染为树形组件）</li>
 *   <li>Redis 缓存管理（Cache Aside 模式）</li>
 * </ul>
 *
 * <h3>Redis 缓存策略 —— Cache Aside 模式：</h3>
 * <ol>
 *   <li><b>读操作（listAll / listByRoleId）：</b>
 *       先查 Redis 缓存 → 命中则直接返回 → 未命中则查数据库 → 将结果写入 Redis 并设置 TTL</li>
 *   <li><b>写操作（add / update / delete）：</b>
 *       先写数据库 → 成功后立即删除所有相关 Redis 缓存键 → 下次读取时自动重建缓存</li>
 * </ol>
 *
 * <h3>缓存 Key 约定：</h3>
 * <ul>
 *   <li>全部菜单列表：menu:all（TTL 60 分钟）</li>
 *   <li>按角色查询菜单：menu:role:{roleId}（TTL 60 分钟）</li>
 * </ul>
 *
 * <h3>菜单树形查询（重要）：</h3>
 * 菜单采用<b>树形结构存储</b>，parentId 字段建立父子关系，sort 字段控制同级排序。
 * listAll() 方法按 parentId ASC, sort ASC 的双重排序返回所有菜单，
 * 前端收到后可直接渲染为树形导航菜单组件，无需额外转换。
 *
 * <h3>设计思路：</h3>
 * <ul>
 *   <li>继承 MyBatis-Plus 的 ServiceImpl 获得基础 CRUD 能力</li>
 *   <li>菜单名称全局唯一，添加时进行唯一性校验</li>
 *   <li>按角色查询菜单通过 sys_role_menu 中间表实现多对多关联</li>
 *   <li>权限变更后清除缓存，确保前端菜单实时更新</li>
 * </ul>
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    // ======================== 缓存 Key 常量 ========================

    /** 全部菜单列表的 Redis 缓存 Key */
    private static final String MENU_ALL_KEY = "menu:all";

    /** 按角色查询菜单的 Redis 缓存 Key 前缀，完整格式为 menu:role:{roleId} */
    private static final String MENU_ROLE_PREFIX = "menu:role:";

    // ======================== 依赖注入 ========================

    /** 菜单数据访问层 */
    @Autowired
    private SysMenuMapper sysMenuMapper;

    /** Redis 模板，用于缓存操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ======================== 写操作（含唯一性校验与缓存清除） ========================

    /**
     * 添加菜单
     *
     * <h3>执行流程：</h3>
     * <ol>
     *   <li><b>唯一性校验：</b>查询数据库中是否已存在同名的菜单名称，防止重复创建</li>
     *   <li><b>写入数据库：</b>执行 INSERT 操作</li>
     *   <li><b>清除缓存：</b>删除所有菜单相关缓存（Cache Aside 写模式）</li>
     * </ol>
     *
     * @param menu 菜单实体，需包含 menuName、parentId、sort 等字段
     * @return 影响行数（>0 表示添加成功）
     * @throws RuntimeException 当菜单名称已存在时抛出
     */
    @Override
    public int add(SysMenu menu) {
        // 菜单名称唯一性校验
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getMenuName, menu.getMenuName());
        if (sysMenuMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("菜单名称已存在");
        }

        // 写入数据库
        int result = sysMenuMapper.insert(menu);

        // 写入成功后删除缓存（Cache Aside 写模式：淘汰缓存而非更新缓存）
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    /**
     * 修改菜单
     *
     * 可修改菜单名称、图标、路由路径、排序、可见性等属性
     * 更新数据库后清除所有菜单相关缓存，保证前端菜单实时更新
     *
     * @param menu 菜单实体，id 字段必填
     * @return 影响行数（>0 表示修改成功）
     */
    @Override
    public int update(SysMenu menu) {
        int result = sysMenuMapper.updateById(menu);
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    /**
     * 删除菜单
     *
     * 物理删除数据库记录后清除缓存
     *
     * @param menuId 菜单ID
     * @return 影响行数（>0 表示删除成功）
     */
    @Override
    public int delete(Long menuId) {
        int result = sysMenuMapper.deleteById(menuId);
        if (result > 0) {
            evictCache();
        }
        return result;
    }

    // ======================== 读操作（Cache Aside 读模式） ========================

    /**
     * 获取所有菜单列表（树形排序） —— Cache Aside 读模式
     *
     * <h3>树形排序规则（重要）：</h3>
     * 查询结果按 <b>parentId ASC, sort ASC</b> 双重排序：
     * <ul>
     *   <li><b>parentId ASC：</b>先按父级ID升序，确保顶级菜单（parentId=0）排在最前，
     *       其次是各父级下的子菜单</li>
     *   <li><b>sort ASC：</b>同级菜单按 sort 字段升序，控制菜单的展示顺序</li>
     * </ul>
     *
     * <h3>前端渲染：</h3>
     * 前端收到此列表后，根据 parentId 字段在内存中构建树形结构，
     * 按 sort 顺序渲染每个节点的子菜单，无需后端递归构建树。
     *
     * <h3>执行流程：</h3>
     * <ol>
     *   <li>读 Redis 缓存 menu:all</li>
     *   <li>缓存命中 → 直接返回</li>
     *   <li>缓存未命中 → 查数据库（ORDER BY parent_id, sort）→ 回写 Redis（TTL 60 分钟）</li>
     * </ol>
     *
     * @return 按 parentId + sort 升序排列的菜单实体列表
     */
    @Override
    public List<SysMenu> listAll() {
        // Step 1: 读缓存
        Object cached = redisTemplate.opsForValue().get(MENU_ALL_KEY);
        if (cached != null) {
            // 缓存命中：直接返回
            return (List<SysMenu>) cached;
        }

        // Step 2: 查数据库
        // 树形排序：先按 parentId 分组，同组内按 sort 排序
        // orderByAsc(SysMenu::getParentId, SysMenu::getSort) 生成 SQL:
        //   ORDER BY parent_id ASC, sort ASC
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysMenu::getParentId, SysMenu::getSort);
        List<SysMenu> list = sysMenuMapper.selectList(wrapper);

        // Step 3: 回写缓存，TTL 60 分钟
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(MENU_ALL_KEY, list, 60, TimeUnit.MINUTES);
        }
        return list;
    }

    /**
     * 按角色ID查询菜单权限 —— Cache Aside 读模式
     *
     * <h3>RBAC 权限模型：</h3>
     * 系统采用 RBAC（基于角色的访问控制）模型：
     * <ul>
     *   <li>用户 → 角色（多对一）</li>
     *   <li>角色 → 菜单（多对多，通过 sys_role_menu 中间表）</li>
     * </ul>
     *
     * <h3>查询原理：</h3>
     * 通过 sysRoleMenuMapper.selectByRoleId(roleId) 查询该角色拥有的菜单列表，
     * 返回的菜单用于前端动态渲染用户可见的导航菜单。
     *
     * @param roleId 角色ID
     * @return 该角色有权限访问的菜单实体列表
     */
    @Override
    public List<SysMenu> listByRoleId(Long roleId) {
        // 构建缓存 Key: menu:role:{roleId}
        String key = MENU_ROLE_PREFIX + roleId;

        // Step 1: 读缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            // 缓存命中：直接返回角色的菜单权限
            return (List<SysMenu>) cached;
        }

        // Step 2: 查数据库
        // sysMenuMapper.selectByRoleId() 内部通过 sys_role_menu 中间表关联查询
        List<SysMenu> list = sysMenuMapper.selectByRoleId(roleId);

        // Step 3: 回写缓存，TTL 60 分钟
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(key, list, 60, TimeUnit.MINUTES);
        }
        return list;
    }

    // ======================== 缓存管理 ========================

    /**
     * 清除所有菜单相关缓存
     *
     * <h3>清除范围：</h3>
     * <ol>
     *   <li>删除 menu:all 键（全部菜单列表缓存）</li>
     *   <li>使用 keys 命令匹配 menu:role:* 并批量删除（所有按角色查询的菜单缓存）</li>
     * </ol>
     *
     * <h3>为什么权限变更要清除所有角色缓存？</h3>
     * 菜单的增删改操作可能影响任意角色的菜单权限（尤其是在删除菜单时，
     * 该菜单可能被多个角色引用），因此需要清除所有 menu:role:* 缓存。
     * 生产环境中若角色数量多，可考虑只清除受影响角色的缓存以提升效率。
     *
     * <h3>为什么删除而非更新？</h3>
     * Cache Aside 模式推荐"删除缓存"而非"更新缓存"：
     * <ul>
     *   <li>删除操作简单，无需维护缓存与数据库的一致性逻辑</li>
     *   <li>下次读取自动从数据库加载最新数据并重建缓存</li>
     *   <li>避免了并发更新缓存可能引发的数据错乱问题</li>
     * </ul>
     */
    private void evictCache() {
        // 删除全部菜单列表缓存
        redisTemplate.delete(MENU_ALL_KEY);
        // 批量删除所有角色菜单权限缓存（通配符匹配 menu:role:*）
        redisTemplate.delete(redisTemplate.keys(MENU_ROLE_PREFIX + "*"));
    }
}
