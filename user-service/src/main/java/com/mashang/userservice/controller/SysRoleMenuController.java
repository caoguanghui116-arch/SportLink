package com.mashang.userservice.controller;

import com.mashang.common.common.R;
import com.mashang.userservice.service.ISysRoleMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 角色菜单关联管理控制器
 *
 * 核心职责：
 * - 分配菜单权限：为指定角色授予某个菜单的访问权限
 * - 移除菜单权限：从指定角色收回某个菜单的访问权限
 *
 * 设计思路：
 * - 这是 RBAC（基于角色的访问控制）模型中角色与权限之间的"多对多"关系管理
 * - 使用中间表 sys_role_menu 存储角色与菜单的关联关系
 * - 权限变更后会清除相关的 Redis 菜单缓存，确保前端导航菜单实时更新
 * - 控制器仅做参数接收和结果封装，实际逻辑在 ISysRoleMenuService 中
 */
@Api(tags = "角色菜单关联")
@RestController
@RequestMapping("/system/role-menu")
public class SysRoleMenuController {

    /** 角色菜单关联服务接口 */
    @Autowired
    private ISysRoleMenuService sysRoleMenuService;

    /**
     * 为角色分配菜单权限
     *
     * 在 sys_role_menu 中间表中插入一条关联记录
     * 操作后清除相关 Redis 缓存以确保权限变更立即生效
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 统一响应体，ok 表示分配成功
     */
    @ApiOperation("分配菜单权限")
    @PostMapping
    @ApiImplicitParam(name = "menuId", value = "菜单ID", required = true)
    public R<Void> assign(@RequestParam Long roleId, @RequestParam Long menuId) {
        return R.toResult(sysRoleMenuService.assign(roleId, menuId));
    }

    /**
     * 移除角色的菜单权限
     *
     * 删除 sys_role_menu 中间表中的指定关联记录
     * 操作后清除相关 Redis 缓存以确保权限变更立即生效
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 统一响应体，ok 表示移除成功
     */
    @ApiOperation("移除菜单权限")
    @DeleteMapping
    @ApiImplicitParam(name = "menuId", value = "菜单ID", required = true)
    public R<Void> remove(@RequestParam Long roleId, @RequestParam Long menuId) {
        return R.toResult(sysRoleMenuService.remove(roleId, menuId));
    }
}
