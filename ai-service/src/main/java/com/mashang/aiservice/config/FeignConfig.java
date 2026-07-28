package com.mashang.aiservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign configuration for JWT token passthrough.
 * Intercepts outgoing Feign requests and attaches the Authorization header
 * from the current HTTP request context.
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {

            // 1. Get current request context
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            // 2. Guard against non-web requests (e.g. async calls)
            if (attributes == null) {
                return;
            }

            jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();

            // 3. Extract token from incoming request
            String token = request.getHeader("Authentication");

            System.out.println("Feign passthrough token = " + token);

            // 4. Forward token to downstream Feign request
            if (token != null && !token.isEmpty()) {
                requestTemplate.header("Authentication", token);
            }
        };
    }
}
