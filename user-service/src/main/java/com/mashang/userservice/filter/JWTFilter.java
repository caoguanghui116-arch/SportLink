package com.mashang.userservice.filter;

import com.mashang.userservice.utils.JWTUtil;
import com.mashang.userservice.utils.LoginUser;
import com.mashang.userservice.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * JWT 认证过滤器 —— 在每个请求到达 Controller 前完成身份校验。
 *
 * 继承 OncePerRequestFilter 的原因：
 * - 保证每个请求只执行一次过滤（即使请求被转发或 include）
 * - 避免在同一个请求中重复解析 JWT 和查询 Redis
 *
 * 安全架构说明（双重鉴权）：
 * - Gateway 层：校验 JWT 签名 + Redis 会话是否有效（全局第一关）
 * - user-service 层：解析用户信息注入 SecurityContext（细粒度权限控制）
 * - Gateway 保证"你是谁"，user-service 保证"你能做什么"
 *
 * 为什么还需要 user-service 层校验：
 * - Gateway 只验证了 JWT 合法性，没有将用户信息注入 Spring Security 上下文
 * - 没有 SecurityContext，@PreAuthorize 和 hasAuthority() 无法工作
 * - 角色权限（admin/referee/athlete）的校验必须在这一层完成
 */
@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // ---- 第1步：获取请求头中的 Token ----
        // 注意：这里读取的是 "Authentication" 头，与 Gateway 的 "Authorization" 不同
        // 实际生产环境中应统一为 "Authorization"
        String Authentication = request.getHeader("Authentication");
        if (!StringUtils.hasText(Authentication)) {
            // 没有 Token 则放行 —— 后续由 SecurityConfig.configure() 的 URL 规则决定是否拒绝
            filterChain.doFilter(request, response);
            return;
        }

        // ---- 第2步：校验 JWT 签名 ----
        boolean b = JWTUtil.verifyToken(Authentication);
        if (!b) {
            throw new RuntimeException("Authentication不合法");
        }

        // ---- 第3步：从 Redis 获取用户会话信息 ----
        Long userId = JWTUtil.getUserId(Authentication);
        LoginUser user = redisUtil.getCacheObject("user:" + userId);

        if (Objects.isNull(user)) {
            throw new RuntimeException("用户未登录或者登陆过期");
        }

        // ---- 第4步：将用户信息注入 Spring Security 上下文 ----
        // 第3个参数传入 user.getAuthorities()（非 null），使 @PreAuthorize 注解生效
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // ---- 第5步：放行，交给后续过滤器链和 Controller ----
        filterChain.doFilter(request, response);
    }
}
