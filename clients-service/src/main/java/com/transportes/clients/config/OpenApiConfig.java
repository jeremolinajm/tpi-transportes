package com.transportes.clients.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Clients Service API",
                version = "1.0.0",
                description = "API para gestión de clientes, contenedores y solicitudes de transporte",
                contact = @Contact(
                        name = "TPI Transportes",
                        email = "support@transportes.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Local Server"),
                @Server(url = "http://localhost:8080", description = "Gateway")
        }
)
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
