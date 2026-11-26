package com.ifpr.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI financeiroApi() {
        return new OpenAPI().info(new Info().title("API de Gestão Financeira Compartilhada")
            .version("1.0").description("Carteiras, transações, relatórios, eventos SSE, insights e orçamento mensal."))
            .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }

    @Bean
    OpenApiCustomizer authenticatedOperations() {
        return api -> {
            if (api.getPaths() == null) return;
            api.getPaths().forEach((path, item) -> {
                if (path.startsWith("/api/")) item.readOperations().forEach(operation ->
                    operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth")));
            });
        };
    }
}
