package com.aiworkorder.ai_workorder_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class Knife4jConfig {

    /**
     *  Knife4j 文档配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI工单智能回答系统 API 文档")
                        .version("1.0")
                        .description("AI工单系统接口文档，使用 Knife4j 构建"));
    }
}