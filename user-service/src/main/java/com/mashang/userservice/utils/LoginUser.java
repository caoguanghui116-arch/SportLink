package com.mashang.userservice.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mashang.userservice.domain.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


@Data
@NoArgsConstructor
@AllArgsConstructor
// 解决后续redis读取数据时反序列化报错
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser implements UserDetails {

    //UserDetails 原生security自带的实体类

    // 将SysUser与SpringSecurity的登录信息相结合
    private SysUser user;//自定义的登陆对象

    //权限相关
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    /**
     * 框架中会自动调用获取用户名和密码的操作，所以返回值要重写一下
     * @return
     */
    @Override
    public String getPassword() {
        return user.getPassword();//换成用户自己的
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     *     布尔值记得改为True，否则可能无法访问，查询账号是否过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     *     判断当前账户是否被锁住
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     *     判断当前密码是否过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     *     判断账户是否可用(是否被删除)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
