package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mashang.userservice.domain.entity.SysUser;
import com.mashang.userservice.utils.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserServiceImpl service;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        // 查询到对应的用户
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = service.getOne(wrapper);
        // 判断用户是否为空
        if (Objects.isNull(user)) {
            throw new RuntimeException("用户不存在");
        }

        // 主要用来存储登录的用户的信息
        return new LoginUser(user);
    }
}
