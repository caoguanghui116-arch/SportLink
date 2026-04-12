package com.mashang.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.userservice.domain.entity.SysMenu;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<com.mashang.userservice.domain.entity.SysMenu> {

    List<SysMenu> selectByRoleId(Long roleId);
}
