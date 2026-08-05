package com.andrew.usage_service.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI usageServiceOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Usage Service API")
                        .description("REST API for Home Energy Tracker")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Andrew")
                                .email("andrew@gmail.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation"));
    }
}
