package com.stand.backend;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH_SCHEME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Stand API - Revenda de Veículos")
                .description("API REST do sistema Stand para catálogo público de veículos, captação de leads e gestão administrativa do estoque.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Equipe Stand")
                    .email("contato@stand.local")))
            .components(new Components()
                .addSecuritySchemes(BEARER_AUTH_SCHEME,
                    new SecurityScheme()
                        .name(BEARER_AUTH_SCHEME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("Token")
                        .description("Insira o token de sessão obtido no endpoint de login POST /api/admin/auth/login. Exemplo: Bearer <token> (informe apenas o token abaixo)")));
    }
}

