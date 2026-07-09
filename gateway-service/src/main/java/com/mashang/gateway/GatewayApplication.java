package com.mashang.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Gateway 网关服务 —— 启动类。
 *
 * 职责：统一入口 / JWT 鉴权 / 路由转发 / 跨域处理
 * 技术选型：Spring Cloud Gateway（基于 WebFlux 响应式，Netty 容器）
 *
 * 注意：不要引入 spring-boot-starter-web，会和 WebFlux 冲突
 */
@SpringBootApplication
@EnableDiscoveryClient  // 向 Nacos 注册自身 & 拉取其他服务实例列表
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
