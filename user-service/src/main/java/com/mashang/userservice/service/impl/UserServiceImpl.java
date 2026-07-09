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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 用户服务实现 —— 认证、注册、密码管理等核心功能。
 *
 * 安全优化：
 * - 密码存储：BCrypt 单向哈希加密（之前为明文存储）
 * - 认证流程：AuthenticationManager.authenticate() 自动调用 BCrypt 比对
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements IUserService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;  // BCryptPasswordEncoder

    /**
     * 用户登录 —— JWT 令牌签发。
     *
     * 认证流程：
     * 1. AuthenticationManager.authenticate() 调用 UserDetailsServiceImpl.loadUserByUsername()
     * 2. DaoAuthenticationProvider 自动用 BCrypt 比对密码
     * 3. 认证通过 → 签发 JWT → 存 Redis 会话 → 返回 Token
     */
    @Override
    public ApiResponse login(LoginUserQuery user) {
        // 构建认证凭据：UsernamePasswordAuthenticationToken（未认证状态）
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

        // authenticate() 内部流程：
        // 1) 调用 UserDetailsServiceImpl.loadUserByUsername() 查用户
        // 2) 用 BCryptPasswordEncoder 比对密码
        // 3) 比对成功返回已认证的 Authentication 对象
        Authentication authenticate = authenticationManager.authenticate(token);

        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("认证失败");
        }

        // 获取登录用户信息 → 签发 JWT
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String jwtToken = JWTUtil.createToken(loginUser.getUser());

        // 将用户信息存入 Redis（用于 Gateway 校验会话状态和登出控制）
        // Key: user:{userId}, Value: LoginUser JSON
        redisUtil.setCacheObject("user:" + loginUser.getUser().getUserId(), loginUser);

        return ApiResponse.ok(jwtToken);
    }

    /**
     * 用户注册 —— 密码 BCrypt 加密存储。
     */
    @Override
    public int register(RegisterUserQuery query) {
        // 用户名唯一性校验
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, query.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(query.getUsername());
        // 密码 BCrypt 加密后再存储（之前为明文存储：query.getPassword()）
        user.setPassword(passwordEncoder.encode(query.getPassword()));
        user.setRealName(query.getRealName());
        user.setPhone(query.getPhone());
        user.setRoleId(query.getRoleId() != null ? query.getRoleId() : 1L);  // 默认角色：管理员
        user.setStatus("0");  // 状态：0=启用，1=停用
        return userMapper.insert(user);
    }

    /**
     * 修改密码 —— BCrypt 加密后更新。
     */
    @Override
    public int update(String password) {
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(SysUser::getUserId, JWTUtil.getUserId())
                // 新密码 BCrypt 加密（之前为明文更新）
                .set(SysUser::getPassword, passwordEncoder.encode(password));

        return userMapper.update(null, updateWrapper);
    }

    /**
     * 查询所有裁判列表 —— JOIN sys_role WHERE role_key = 'referee'
     */
    @Override
    public List<RefereeVo> allReferee() {
        return userMapper.allReferee();
    }
}
