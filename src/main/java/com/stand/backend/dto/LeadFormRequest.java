package com.stand.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Formulário de envio de proposta/lead de interesse pelo cliente")
public record LeadFormRequest(
        @Schema(description = "Nome completo do cliente interessado", example = "Carlos Silva")
        String customerName,
        @Schema(description = "Telefone ou WhatsApp de contato com DDD", example = "(11) 98765-4321")
        String phone,
        @Schema(description = "E-mail de contato", example = "carlos.silva@email.com")
        String email,
        @Schema(description = "Mensagem ou proposta enviada pelo cliente", example = "Olá, tenho interesse neste veículo. Gostaria de agendar uma visita.")
        String message
) {
}