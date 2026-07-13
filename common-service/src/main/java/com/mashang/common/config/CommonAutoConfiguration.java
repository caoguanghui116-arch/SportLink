package com.mashang.common.config;

import com.mashang.common.handler.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * common-service 自动配置入口 —— 通过 spring.factories 实现零配置自动装配。
 *
 * 工作原理：
 * 1. 各子服务在 pom.xml 中引入 common-service 依赖
 * 2. Spring Boot 启动时扫描 common-service.jar 中的 META-INF/spring.factories
 * 3. 发现 EnableAutoConfiguration = CommonAutoConfiguration
 * 4. 自动加载本类中 @Import 的所有组件
 *
 * @Import 导入的组件：
 * - Knife4jConfig.class         → Knife4j / Swagger 接口文档自动配置
 * - GlobalExceptionHandler.class → 全局异常处理器（统一拦截异常并返回 R）
 *
 * 设计优势：
 * - 子服务无需写任何 @EnableSwagger2WebMvc 或 @RestControllerAdvice
 * - 新增公共组件只需在 @Import 中添加，所有服务自动生效
 * - 如果某个服务不需要某个组件，可以在子服务中排除该自动配置
 *
 * 注意：
 * - 本类只负责导入，不包含具体的 Bean 定义
 * - 具体的 Bean 定义在各自的 @Configuration 类中（如 Knife4jConfig）
 * - spring.factories 文件路径：common-service/resources/META-INF/spring.factories
 */
@Configuration
@Import({Knife4jConfig.class, GlobalExceptionHandler.class})
public class CommonAutoConfiguration {
}
