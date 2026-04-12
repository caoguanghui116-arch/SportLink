package com.mashang.userservice.controller;

import com.mashang.userservice.domain.entity.R;
import com.mashang.userservice.service.ISysRoleMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "角色菜单关联")
@RestController
@RequestMapping("/system/role-menu")
public class SysRoleMenuController {

    @Autowired
    private ISysRoleMenuService sysRoleMenuService;

    @ApiOperation("分配菜单权限")
    @PostMapping("/assign")
    public R assign(@RequestParam Long roleId, @RequestParam Long menuId) {
        return R.toResult(sysRoleMenuService.assign(roleId, menuId));
    }

    @ApiOperation("移除菜单权限")
    @DeleteMapping("/remove")
    public R remove(@RequestParam Long roleId, @RequestParam Long menuId) {
        return R.toResult(sysRoleMenuService.remove(roleId, menuId));
    }
}
