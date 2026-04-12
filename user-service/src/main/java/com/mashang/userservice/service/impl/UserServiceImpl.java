package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.SysUser;
import com.mashang.userservice.domain.query.LoginUserQuery;
import com.mashang.userservice.domain.query.create.RegisterUserQuery;
import com.mashang.userservice.domain.vo.RefereeVo;
import com.mashang.userservice.mapper.UserMapper;
import com.mashang.userservice.service.IUserService;
import com.mashang.userservice.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements IUserService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserMapper userMapper;
    @Override
    public ApiResponse login(LoginUserQuery user) {

        //AuthenticationManager.authenticate()进行用户认证
        // 需要一个参数:Authentication的实现类，所以需要把user转换成Authentication的实现类
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

        //authenticate内部会自动调用UserDetailsServiceImpl的loadUserByUsername方法
        Authentication authenticate = authenticationManager.authenticate(token);

        // 如果认证没通过就给出对应的提示
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("认证失败");
        }

        // 如果通过则根据SysUser生成jwt
        // 从上下文凭证获取登录的用户信息
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String Authentication = JWTUtil.createToken(loginUser.getUser());

        // 将用户信息存入redis
        redisUtil.setCacheObject("user:" + loginUser.getUser().getUserId(), loginUser);
        //把token响应给前端
        return ApiResponse.ok(Authentication);
    }

    @Override
    public int register(RegisterUserQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, query.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(query.getUsername());
        user.setPassword(query.getPassword());
        user.setRealName(query.getRealName());
        user.setPhone(query.getPhone());
        user.setRoleId(query.getRoleId() != null ? query.getRoleId() : 1L);
        user.setStatus("0");
        return userMapper.insert(user);
    }

    @Override
    public int update(String password) {

        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(SysUser::getUserId, JWTUtil.getUserId())
                .set(SysUser::getPassword, password);

        return userMapper.update(null, updateWrapper);
    }

    @Override
    public List<RefereeVo> allReferee() {

        return userMapper.allReferee();
    }
}
