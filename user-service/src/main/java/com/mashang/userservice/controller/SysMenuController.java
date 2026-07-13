package com.mashang.userservice.controller;

import com.mashang.common.common.R;
import com.mashang.userservice.domain.entity.SysMenu;
import com.mashang.userservice.service.ISysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统菜单管理控制器
 *
 * 核心职责：
 * - 菜单增删改查：管理系统左侧导航菜单的层级结构
 * - 按角色查询菜单：根据角色ID获取该角色有权限访问的菜单列表（RBAC 权限模型）
 *
 * 设计思路：
 * - 菜单采用树形结构存储，通过 parentId 字段建立父子关系，sort 字段控制同级排序
 * - 查询菜单列表时按 parentId + sort 升序排列，前端直接渲染为树形组件
 * - 菜单名称全局唯一，添加时进行唯一性校验
 * - 菜单数据使用 Redis 缓存，写操作后清除缓存以保证数据一致性
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    /** 菜单服务接口 */
    @Autowired
    private ISysMenuService sysMenuService;

    /**
     * 添加菜单
     *
     * 添加前会校验菜单名称是否已存在（唯一性约束）
     *
     * @param menu 菜单实体，需包含 menuName、parentId、sort 等字段
     * @return 统一响应体，ok 表示添加成功
     */
    @ApiOperation("添加菜单")
    @PostMapping
    public R<Void> add(@RequestBody @Validated SysMenu menu) {
        return R.toResult(sysMenuService.add(menu));
    }

    /**
     * 修改菜单
     *
     * 可修改菜单名称、图标、路径、排序等属性
     *
     * @param menu 菜单实体，id 字段必填用于定位待更新记录
     * @return 统一响应体，ok 表示修改成功
     */
    @ApiOperation("修改菜单")
    @PutMapping
    public R<Void> update(@RequestBody @Validated SysMenu menu) {
        return R.toResult(sysMenuService.update(menu));
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 统一响应体，ok 表示删除成功
     */
    @ApiOperation("删除菜单")
    @DeleteMapping("/{menuId}")
    @ApiImplicitParam(name = "menuId", value = "菜单ID", required = true)
    public R<Void> delete(@PathVariable Long menuId) {
        return R.toResult(sysMenuService.delete(menuId));
    }

    /**
     * 获取所有菜单列表
     *
     * 返回的列表按 parentId + sort 升序排列，前端可直接渲染为树形菜单
     * 查询使用 Redis 缓存（Cache Aside 模式），TTL 60 分钟
     *
     * @return 统一响应体，data 字段为树形排序后的菜单列表
     */
    @ApiOperation("菜单列表")
    @GetMapping
    public R<List<SysMenu>> list() {
        return R.ok(sysMenuService.listAll());
    }

    /**
     * 根据角色ID查询该角色可见的菜单
     *
     * 使用场景：用户登录后，前端根据用户角色查询对应的导航菜单
     * 通过 SysRoleMenu 关联表确定角色拥有哪些菜单的访问权限
     *
     * @param roleId 角色ID
     * @return 统一响应体，data 字段为该角色有权限的菜单列表
     */
    @ApiOperation("根据角色查询菜单")
    @GetMapping("/role/{roleId}")
    @ApiImplicitParam(name = "roleId", value = "角色ID", required = true)
    public R<List<SysMenu>> listByRole(@PathVariable Long roleId) {
        return R.ok(sysMenuService.listByRoleId(roleId));
    }
}
