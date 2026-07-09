package com.mashang.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gateway 全局鉴权过滤器 —— 所有请求进网关后第一关。
 *
 * 执行流程：
 * 1. 检查请求路径是否在白名单中（白名单直接放行）
 * 2. 从 Authorization 头获取 JWT Token
 * 3. 校验 JWT 签名和过期时间
 * 4. 从 Redis 校验会话是否仍然有效
 * 5. 将 userId 写入 X-User-Id 请求头，透传给下游微服务
 *
 * 设计意图：鉴权逻辑集中在网关层，业务服务不再各自校验 JWT，降低耦合
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /** 鉴权白名单路径列表（从 application.yml 的 gateway.auth.whitelist 注入） */
    @Value("${gateway.auth.whitelist:}")
    private List<String> whitelist;

    /** Redis 客户端：校验 JWT 对应的会话是否仍然有效（用户登出后会删除 Redis 中的会话） */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** JWT 签名密钥（与 user-service 的 JWTUtil 保持一致） */
    private static final String JWT_SECRET = "chiwhvbsugiw";

    /** Ant 风格路径匹配器：支持 /user/** 通配符 */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 全局过滤逻辑 —— 每个请求都会执行
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // ---- 第1步：白名单放行（登录、注册、文档等） ----
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // ---- 第2步：获取 Token ----
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || token.isEmpty()) {
            return unauthorizedResponse(exchange, "未提供认证令牌");
        }

        // ---- 第3步：校验 JWT 签名和过期时间 ----
        Long userId;
        try {
            userId = JWT.require(Algorithm.HMAC256(JWT_SECRET))
                    .build()
                    .verify(token)
                    .getClaim("userId")       // 从 JWT Payload 提取 userId
                    .asLong();
        } catch (JWTVerificationException e) {
            log.warn("JWT verification failed: {}", e.getMessage());
            return unauthorizedResponse(exchange, "令牌无效或已过期");
        }

        // ---- 第4步：Redis 会话校验（防止用户登出后令牌仍然可用） ----
        String redisKey = "user:" + userId;
        String sessionData = stringRedisTemplate.opsForValue().get(redisKey);
        if (sessionData == null) {
            return unauthorizedResponse(exchange, "用户未登录或会话已过期");
        }

        // ---- 第5步：透传 userId 给下游微服务（X-User-Id 请求头） ----
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 判断路径是否在白名单中（支持 Ant 通配符匹配）
     */
    private boolean isWhitelisted(String path) {
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回 401 未授权 JSON 响应（不将请求转发到下游）
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构建统一响应体：{ code: 401, msg: "xxx" }
        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("msg", message);

        byte[] bytes = JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 过滤器执行顺序：值越小越优先
     * -100 确保鉴权过滤器在所有自定义过滤器之前执行
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
