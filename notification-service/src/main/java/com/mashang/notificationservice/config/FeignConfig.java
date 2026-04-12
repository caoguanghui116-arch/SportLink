package com.mashang.notificationservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {

            // 1. 获取当前请求上下文
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            // 2. 防止非 Web 请求（比如异步调用时报空）
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            // 3. 获取 token
            String token = request.getHeader("Authorization");

            System.out.println("Original token = " + token);
            // 4. 透传 token 到 Feign 请求
            if (token != null && !token.isEmpty()) {
                requestTemplate.header("Authorization", token);
            }
        };
    }
}
