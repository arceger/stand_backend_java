package com.stand.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciais para login administrativo")
public record LoginRequest(
        @Schema(description = "E-mail cadastrado do administrador", example = "admin@stand.local")
        String email,
        @Schema(description = "Senha de acesso", example = "Admin123!")
        String password
) {
}