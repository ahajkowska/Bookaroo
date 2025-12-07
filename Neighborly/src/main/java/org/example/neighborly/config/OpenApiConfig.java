package org.example.neighborly.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI neighborlyOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Neighborly API")
                        .description("API do zarządzania platformą wymiany usług lokalnych")
                        .version("v1")
                        .contact(new Contact().name("Neighborly").email("neighobrly@gmail.com"))
                );
    }
}