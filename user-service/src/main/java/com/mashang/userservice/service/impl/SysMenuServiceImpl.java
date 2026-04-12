package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.SysMenu;
import com.mashang.userservice.mapper.SysMenuMapper;
import com.mashang.userservice.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Override
    public int add(SysMenu menu) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getMenuName, menu.getMenuName());
        if (sysMenuMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("菜单名称已存在");
        }
        return sysMenuMapper.insert(menu);
    }

    @Override
    public int update(SysMenu menu) {
        return sysMenuMapper.updateById(menu);
    }

    @Override
    public int delete(Long menuId) {
        return sysMenuMapper.deleteById(menuId);
    }

    @Override
    public List<SysMenu> listAll() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysMenu::getParentId, SysMenu::getSort);
        return sysMenuMapper.selectList(wrapper);
    }

    @Override
    public List<SysMenu> listByRoleId(Long roleId) {
        return sysMenuMapper.selectByRoleId(roleId);
    }
}
