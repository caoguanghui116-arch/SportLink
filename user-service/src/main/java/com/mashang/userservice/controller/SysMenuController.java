package com.mashang.userservice.controller;

import com.mashang.userservice.domain.entity.R;
import com.mashang.userservice.domain.entity.SysMenu;
import com.mashang.userservice.service.ISysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "菜单管理")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Autowired
    private ISysMenuService sysMenuService;

    @ApiOperation("添加菜单")
    @PostMapping("/add")
    public R add(@RequestBody SysMenu menu) {
        return R.toResult(sysMenuService.add(menu));
    }

    @ApiOperation("修改菜单")
    @PutMapping("/update")
    public R update(@RequestBody SysMenu menu) {
        return R.toResult(sysMenuService.update(menu));
    }

    @ApiOperation("删除菜单")
    @DeleteMapping("/delete/{menuId}")
    @ApiImplicitParam(name = "menuId", value = "菜单ID")
    public R delete(@PathVariable Long menuId) {
        return R.toResult(sysMenuService.delete(menuId));
    }

    @ApiOperation("菜单列表")
    @GetMapping("/list")
    public R<List<SysMenu>> list() {
        return R.ok(sysMenuService.listAll());
    }

    @ApiOperation("根据角色查询菜单")
    @GetMapping("/role/{roleId}")
    @ApiImplicitParam(name = "roleId", value = "角色ID")
    public R<List<SysMenu>> listByRole(@PathVariable Long roleId) {
        return R.ok(sysMenuService.listByRoleId(roleId));
    }
}
