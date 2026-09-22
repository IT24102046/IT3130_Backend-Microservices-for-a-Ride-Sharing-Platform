package com.ridelink.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI farePaymentOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink Fare & Payment Service")
                        .description("API documentation for RideLink fare estimation and simulated payment features.")
                        .version("v1"));
    }
}