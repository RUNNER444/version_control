package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {        
        return new OpenAPI()
            .info(new Info()
                .title("Version Control API")
                .description("REST API for version control and management. " +
                           "This API allows you to track app versions across different platforms " +
                           "and monitor their usage in user devices.")
                .version("1.3.3.7"));
    }
}