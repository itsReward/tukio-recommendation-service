package com.tukio.recommendationservice.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Campus Event Scheduling - Recommendation Service API")
                    .description("API documentation for the Recommendation Service microservice that provides personalized event recommendations")
                    .version("v1.0")
                    .contact(
                        Contact()
                            .name("Campus Events Team")
                            .email("events@campus.edu")
                    )
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0")
                    )
            )
            .addServersItem(
                Server()
                    .url("/")
                    .description("Local development server")
            )
    }
}