package com.productos.demo.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI 3.0 / Swagger
 * Define la documentación de la API
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Gestión de Productos API")
                .version("1.0.0")
                .description("API RESTful para la gestión de productos con enfoque Contract-First. " +
                    "Implementa un CRUD completo con validaciones, paginación y manejo estandarizado de errores.")
                .contact(new Contact()
                    .name("Equipo de Desarrollo")
                    .email("dev@productos.com"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")));
    }
}
