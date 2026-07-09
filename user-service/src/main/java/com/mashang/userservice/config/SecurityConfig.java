package com.mashang.userservice.config;

import com.mashang.userservice.filter.JWTFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 核心配置类。
 *
 * 安全策略：
 * 1. 基于 JWT 的无状态认证 —— 不依赖服务端 Session，每次请求携带 Token
 * 2. BCrypt 密码加密 —— 相同的明文每次加密结果不同，抵御彩虹表攻击
 * 3. 角色权限控制 —— admin / referee / athlete 三级权限，接口级别隔离
 * 4. 白名单机制 —— 登录、注册、API 文档允许匿名访问
 *
 * 架构说明：
 * - 鉴权分为两级：Gateway（统一校验 JWT 合法性） + user-service（细粒度角色权限）
 * - Gateway 负责"你是谁"，user-service 负责"你能做什么"
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)  // 开启 @PreAuthorize / @PostAuthorize 方法级鉴权
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JWTFilter jwtFilter;

    /**
     * 密码编码器 —— BCrypt 单向哈希算法。
     *
     * 为什么选 BCrypt：
     * - 自带盐值（salt），相同密码每次 hash 结果不同，抵御彩虹表
     * - 计算速度故意偏慢（约 100ms），增加暴力破解成本
     * - Spring Security 官方推荐
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 将 AuthenticationManager 暴露为 Spring Bean。
     * 用于 UserServiceImpl.login() 中手动调用 authenticate()
     *
     * @return AuthenticationManager 实例
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * HTTP 安全配置 —— 核心规则定义
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // ---- CSRF 防护 ----
                // 关闭 CSRF：JWT 无状态架构不需要 CSRF Token（非 Cookie 鉴权）
                .csrf().disable()

                // ---- 会话管理 ----
                // 无状态：不创建 HttpSession，每次请求独立校验 JWT
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // ---- URL 权限配置 ----
                .authorizeRequests()
                // 白名单：登录、注册（匿名访问）
                .antMatchers("/user/login", "/user/register").permitAll()
                // 白名单：API 文档（开发阶段）
                .antMatchers("/doc.html", "/swagger-ui.html", "/swagger-resources/**",
                        "/webjars/**", "/v2/api-docs/**", "/v3/api-docs/**").permitAll()
                // 管理员专属：系统管理接口（菜单、角色配置）
                .antMatchers("/system/**").hasAuthority("admin")
                // 裁判 & 管理员：用户中心（运动员管理、部门管理、团队管理）
                .antMatchers("/usercenter/**").hasAnyAuthority("admin", "referee")
                // 其他接口：只需要已认证即可（不限制角色）
                .anyRequest().authenticated()
                .and()

                // ---- 自定义过滤器 ----
                // JWTFilter 在 UsernamePasswordAuthenticationFilter 之前执行
                // 在 Spring Security 的账号密码认证之前，先用 JWT 完成认证
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
