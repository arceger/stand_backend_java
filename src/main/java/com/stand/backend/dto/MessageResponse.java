package com.stand.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta padrão de confirmação de mensagem")
public record MessageResponse(
        @Schema(description = "Mensagem descritiva da operação", example = "Interesse enviado com sucesso. A loja entrara em contato em breve.")
        String message
) {
}
