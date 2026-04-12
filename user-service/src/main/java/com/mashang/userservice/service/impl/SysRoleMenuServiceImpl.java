package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.SysRoleMenu;
import com.mashang.userservice.mapper.SysRoleMenuMapper;
import com.mashang.userservice.service.ISysRoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements ISysRoleMenuService {

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public int assign(Long roleId, Long menuId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId)
                .eq(SysRoleMenu::getMenuId, menuId);
        if (sysRoleMenuMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("该角色已拥有此菜单权限");
        }
        SysRoleMenu entity = new SysRoleMenu();
        entity.setRoleId(roleId);
        entity.setMenuId(menuId);
        return sysRoleMenuMapper.insert(entity);
    }

    @Override
    public int remove(Long roleId, Long menuId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId)
                .eq(SysRoleMenu::getMenuId, menuId);
        return sysRoleMenuMapper.delete(wrapper);
    }
}
