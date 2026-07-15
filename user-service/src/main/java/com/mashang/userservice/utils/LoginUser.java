package com.mashang.userservice.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mashang.userservice.domain.entity.SysUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spring Security 登录用户封装 —— 实现 UserDetails 接口。
 *
 * 作用：
 * 1. 桥接系统用户实体 (SysUser) 与 Spring Security 的认证体系
 * 2. 提供角色权限信息（admin / referee / athlete）
 * 3. 支持 Redis 序列化存储（@JsonIgnoreProperties 防止反序列化失败）
 *
 * 角色体系说明：
 * - admin    (roleId=1)：管理员，拥有所有权限
 * - referee  (roleId=2)：裁判，可管理运动员、查看/录入成绩
 * - athlete  (roleId=3)：运动员，查看赛程、报名、查看成绩
 * - 权限继承：高等级角色自动拥有低等级角色的所有权限
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // Redis 反序列化时忽略未知字段
public class LoginUser implements UserDetails {

    /** 系统用户实体 */
    private SysUser user;

    public LoginUser(SysUser user) {
        this.user = user;
    }

    /**
     * 获取用户权限集合 —— 根据 roleId 授予对应的 GrantedAuthority。
     *
     * 权限继承规则：
     * - admin (roleId=1) → [admin, referee, athlete]  // 管理员拥有全部权限
     * - referee (roleId=2) → [referee, athlete]         // 裁判拥有自己和运动员的权限
     * - athlete (roleId=3) → [athlete]                  // 运动员仅有自己的权限
     *
     * 注意：每新增一个角色等级，需要在此处添加对应的 case 分支
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    // ==================== UserDetails 接口实现 ====================

    /** 返回密码（给 AuthenticationManager 认证用） */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /** 返回用户名（登录凭据） */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /** 账户是否未过期 */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 账户是否未锁定 */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 密码是否未过期 */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否可用 —— 根据 status 字段判断。
     * status = "0"：正常启用
     * status = "1"：停用（禁止登录）
     */
    @Override
    public boolean isEnabled() {
        return "0".equals(user.getStatus());
    }
}
