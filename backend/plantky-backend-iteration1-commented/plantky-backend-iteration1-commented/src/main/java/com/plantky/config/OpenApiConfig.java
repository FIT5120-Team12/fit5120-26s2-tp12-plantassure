package com.plantky.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger 文档配置。
 *
 * <p>项目使用 springdoc-openapi 自动扫描 Controller，生成 OpenAPI JSON。
 * Apifox 可以直接导入 {@code /v3/api-docs}，前端/测试人员不需要手动重复维护接口定义。</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * 自定义 OpenAPI 文档的基础元数据。
     *
     * @return 注册到 Spring 容器的 OpenAPI Bean
     */
    @Bean
    public OpenAPI plantkyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plantky Iteration 1 API")
                        .version("v1")
                        .description(
                                "Plant search, local occurrence, environmental weed risk and recommendation APIs."));
    }
}
