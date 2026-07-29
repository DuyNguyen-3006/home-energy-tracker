package com.andrew.user_service.infrastructure.configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("REST API for Home Energy Tracker")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Andrew")
                                .email("andrew@gmail.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation"));
    }

    private static License getLicense() {
        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("http://springdoc.org");
        return license;
    }

    private static Contact getContact() {
        Contact contact = new Contact();
        contact.setUrl("https://github.com/andrew");
        contact.setEmail("phduynguyen306@gmail.com");
        return contact;
    }
}
