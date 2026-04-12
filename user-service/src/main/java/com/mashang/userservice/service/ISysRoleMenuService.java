package com.mashang.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.userservice.domain.entity.SysRoleMenu;

public interface ISysRoleMenuService extends IService<SysRoleMenu> {

    int assign(Long roleId, Long menuId);

    int remove(Long roleId, Long menuId);
}
