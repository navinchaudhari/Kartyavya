package com.kartyavya.email.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI openAPI() {

        return new OpenAPI()

                .info(new Info()

                        .title("Kartyavya Email Notification Service API")

                        .version("1.0.0")

                        .description(
                                "REST APIs for Email Notification Service")

                        .contact(new Contact()

                                .name("Member 4")

                                .email("member4@kartyavya.com")))

                .externalDocs(new ExternalDocumentation()

                        .description("Kartyavya Project Documentation"));
    }

}