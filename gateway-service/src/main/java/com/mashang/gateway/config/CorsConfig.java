package com.mashang.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 跨域 (CORS) 配置 —— Gateway 响应式版本。
 *
 * 为什么在网关层统一配置 CORS：
 * - 前端（浏览器）访问后端不同端口/域名时会触发跨域限制
 * - 在网关层统一处理，业务服务无需各自配置
 * - 使用 WebFlux 响应式 CorsWebFilter（区别于 Spring MVC 的 CorsFilter）
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有来源（开发阶段；生产环境应限制为具体域名）
        config.addAllowedOriginPattern("*");
        // 允许所有请求头（包括自定义头如 Authorization、X-User-Id）
        config.addAllowedHeader("*");
        // 允许所有 HTTP 方法（GET / POST / PUT / DELETE / OPTIONS）
        config.addAllowedMethod("*");
        // 允许携带 Cookie（前端 withCredentials: true 场景）
        config.setAllowCredentials(true);
        // 预检请求(OPTIONS)缓存时间：3600秒 = 1小时
        config.setMaxAge(3600L);

        // 注册到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
