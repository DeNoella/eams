package com.eams.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI eamsOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("E-Asset Management System API")
                .description("Enterprise IT Asset Management Platform - REST API Documentation")
                .version("1.0.0")
                .contact(new Contact().name("E-AMS Support").email("mutesideno@gmail.com")))
            .servers(List.of(
                new Server().url("http://localhost:8081").description("Development"),
                new Server().url("https://api.eams.yourdomain.com").description("Production")
            ));
    }
}
