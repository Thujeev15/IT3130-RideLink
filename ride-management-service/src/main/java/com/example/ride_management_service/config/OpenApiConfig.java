package com.example.ride_management_service.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rideManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ride Management Service API")
                        .description("Ride lifecycle, assignment, retrieval, acceptance, start, completion, and cancellation endpoints for RideLink.")
                        .version("v1")
                        .license(new License().name("Educational use")))
                .externalDocs(new ExternalDocumentation().description("RideLink assignment"));
    }
}