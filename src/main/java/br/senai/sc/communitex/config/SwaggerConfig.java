package br.senai.sc.communitex.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Communitex API")
                        .version("1.0.0")
                        .description("API do projeto Communitex - Sistema de Gestão para adocão de praças"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("communitex-public")
                .packagesToScan("br.senai.sc.communitex.controller")
                .pathsToMatch("/api/**", "/adocoes/**", "/representantes/**")
                .build();
    }
}
