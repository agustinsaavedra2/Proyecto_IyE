package com.backendie.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BackendIE API")
                        .version("1.0.0")
                        .description("API REST para Auditorías Automatizadas de Cumplimiento para MiPymes")
                        .contact(new Contact().name("Equipo BackendIE").email("duvan@example.com"))
                );
    }
}

