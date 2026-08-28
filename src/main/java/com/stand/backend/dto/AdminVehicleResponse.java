package com.stand.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Item resumido de veículo para tabela do painel administrativo")
public record AdminVehicleResponse(
        @Schema(description = "ID único (UUID) do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        @Schema(description = "Slug amigável", example = "honda-civic-2022-exl")
        String slug,
        @Schema(description = "Título do veículo", example = "Honda Civic 2.0 EXL Automático")
        String title,
        @Schema(description = "Preço formatado em EUR (€)", example = "139.900,00 €")
        String price,
        @Schema(description = "Status do veículo", example = "PUBLISHED")
        String status,
        @Schema(description = "Indica se está destacado", example = "true")
        boolean featured,
        @Schema(description = "URL da imagem de capa", example = "/uploads/civic-cover.jpg")
        String coverImageUrl
) {
}
