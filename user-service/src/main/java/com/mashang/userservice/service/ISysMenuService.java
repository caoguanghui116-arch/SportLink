package com.mashang.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.userservice.domain.entity.SysMenu;

import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {

    int add(SysMenu menu);

    int update(SysMenu menu);

    int delete(Long menuId);

    List<SysMenu> listAll();

    List<SysMenu> listByRoleId(Long roleId);
}
