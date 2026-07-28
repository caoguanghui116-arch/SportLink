package com.mashang.scoreservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
/*
Feign 请求拦截器配置
当一个服务通过 Feign 调用另一个服务时，把当前用户请求里的认证信息（比如用户 ID、Token）自动带到下游服务。

 */
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        /*requestTemplate ->Lambda 写法
        实际等价于return new RequestInterceptor(){
            @Override
            public void apply(RequestTemplate requestTemplate){

    }

};
         */
        return requestTemplate -> {

            // 1. 获取当前请求上下文
            //这里注册了一个 Feign 的请求拦截器，RequestInterceptor 是 OpenFeign 提供的接口。作用是在 Feign 发请求之前，执行这里面的代码。

            /*
             * 正常 Feign 请求：
             *
             * 服务A
             *  |
             *  | Feign调用
             *  ↓
             * 服务B
             *
             *
             * 实际发送之前：
             *
             * Feign准备发送请求
             *         |
             *         ↓
             * RequestInterceptor执行
             *         |
             *         ↓
             * 添加header
             *         |
             *         ↓
             * 发送请求
             */
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            // 2. 防止非 Web 请求（比如异步调用时报空）
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            // 3. 透传认证相关 header 到 Feign 请求
//            String userId = request.getHeader("X-User-Id");
//            if (userId != null && !userId.isEmpty()) {
//                requestTemplate.header("X-User-Id", userId);
//            }

            //身份认证，让 Spring Security 判断你是谁
            String authToken = request.getHeader("Authentication");
            if (authToken != null && !authToken.isEmpty()) {
                requestTemplate.header("Authentication", authToken);
            }
        };
    }
}
