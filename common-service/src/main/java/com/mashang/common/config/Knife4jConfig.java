package com.mashang.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * Knife4j (Swagger2) 自动配置 —— 统一生成所有微服务的 API 文档。
 *
 * 架构说明：
 * - 本类定义在 common-service，通过 CommonAutoConfiguration 导入
 * - 所有子服务只需引入 common-service 依赖，无需单独配置 Swagger
 * - 扫描 com.mashang 包下所有 @RestController，自动生成文档
 *
 * 访问方式（各服务端口不同）：
 * - Knife4j UI：http://{host}:{port}/doc.html
 * - Swagger JSON：http://{host}:{port}/v2/api-docs
 *
 * 配置要点：
 * - @EnableSwagger2WebMvc：启用 Swagger 2 自动配置（Springfox 实现）
 * - basePackage("com.mashang")：扫描所有子包，覆盖所有微服务
 * - PathSelectors.any()：不限制路径，所有接口都生成文档
 *
 * 注意：
 * - ai-service 使用 Spring Boot 4.x + Knife4j 4.x（OpenAPI 3），不走此配置
 * - 生产环境可通过 knife4j.enable: false 关闭文档
 */
@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {

    /**
     * 创建 Docket Bean —— Swagger 的核心配置对象。
     * Docket 负责定义要扫描哪些接口、使用哪种 API 协议（Swagger 2）、
     * 以及 API 文档的基本信息。
     *
     * @return Docket 实例，Spring 容器自动管理其生命周期
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())                        // API 文档的标题、描述、版本等基本信息
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.mashang"))  // 扫描 com.mashang 包下所有 Controller
                .paths(PathSelectors.any())               // 所有路径都生成文档，不做过滤
                .build();
    }

    /**
     * 构建 API 文档的基本信息。
     * 这些信息会展示在 Knife4j UI 的首页顶部。
     *
     * @return ApiInfo 对象
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("SportLink 校园运动会管理平台 API")
                .description("基于 RESTful 规范，提供用户、赛事、报名、成绩、社交、通知、AI 等服务的统一接口文档")
                .version("1.0.0")
                .contact(new Contact("SportLink Team", "", ""))
                .build();
    }
}
