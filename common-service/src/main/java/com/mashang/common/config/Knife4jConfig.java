package com.mashang.common.config;

import io.swagger.models.auth.In;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.ArrayList;
import java.util.List;

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
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.mashang"))
                .paths(PathSelectors.any())
                .build()
                // 缺失的两行鉴权挂载代码！
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * 构建 API 文档的基本信息。
     * 这些信息会展示在 Knife4j UI 的首页顶部。
     *
     * @return ApiInfo 对象
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("SportLink 赛讯通平台")
                .description("基于 RESTful 规范，提供用户、赛事、报名、成绩、社交、通知、AI 等服务的统一接口文档")
                .version("1.0.0")
                .contact(new Contact("CGH", "", "你猜"))
                .build();
    }

//     * 安全模式，这里指定token通过Authorization头请求头传递
//     * 告诉 Swagger 页面，你的系统鉴权 Token 是放在 HTTP 请求头 Authorization 里传递的，
//     * 并且在页面上提供一个输入框让你填 Token，发请求时自动带上这个请求头。
//     */


    //只做一件事：告诉 Swagger，你的 Token 存在HTTP 请求头 Authorization里，页面右上角会出现 Authorize 输入框让你填 Token。
    //它只是定义了有这么一套鉴权方式，但没说这套鉴权要作用在哪些接口。
    private List<SecurityScheme> securitySchemes()
    {
        List<SecurityScheme> apiKeyList = new ArrayList<SecurityScheme>();
        apiKeyList.add(new ApiKey("Authorization", "Authorization", In.HEADER.toValue()));
        return apiKeyList;
    }

    /**
     * 安全上下文
     */
    //它的职责是：把上面定义好的 Authorization 鉴权规则，绑定到指定接口路径上。
    //没有这段：就算你在 Swagger 填了 Token，发请求也不会自动带上 Authorization 请求头，所有需要登录的接口都会 403；
    //有这段：匹配到的接口，发起调试请求时自动附加你填好的 Token 头。
    private List<SecurityContext> securityContexts()
    {
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .forPaths(PathSelectors.any())
                        .build());
        return securityContexts;
    }

    /**
     * 默认的安全上引用
     */
    //搭起「Token 鉴权规则」和「接口生效范围」之间的桥梁
    private List<SecurityReference> defaultAuth()
    {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference("Authorization", authorizationScopes));
        return securityReferences;
    }
}
