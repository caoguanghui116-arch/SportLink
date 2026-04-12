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

//OncePerRequestFilter 确报过滤器在每个请求中只执行一次
//拦截请求 & 校验登录（过滤器）
@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 获取token
        String Authentication = request.getHeader("Authentication");
        if (!StringUtils.hasText(Authentication)) {
            // 没有token则放行，有的接口不需要token，不需要解析
            filterChain.doFilter(request, response);
            return;
        }
        // 解析token
        boolean b = JWTUtil.verifyToken(Authentication);
        if (!b) {
            throw new RuntimeException("Token不合法");
        }
        // redis中获取用户信息
        Long userId = JWTUtil.getUserId(Authentication);
        LoginUser user = redisUtil.getCacheObject("user:" + userId);

        if (Objects.isNull(user)) {
            throw new RuntimeException("用户未登录或者登陆过期");
        }

        // SecurityContextHolder.getContext().setAuthentication()需要一个授权对象，所以要先创建一个
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, null, null);

        // 将用户信息存入SecurityContextHolder容器中
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}

