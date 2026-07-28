package com.kartyavya.access.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accessAdminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kartyavya Access & Administration Service API")
                        .description("Authentication, user, department, officer and routing APIs for Kartyavya")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local Access Service")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                        .addSecuritySchemes("internalServiceKey",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-Internal-Service-Key")
                        )
                );
    }
}
