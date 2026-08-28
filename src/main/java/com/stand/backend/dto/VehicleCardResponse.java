package com.stand.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Card resumido de veículo para vitrine e listagem pública")
public record VehicleCardResponse(
        @Schema(description = "ID único (UUID) do veículo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        @Schema(description = "Slug amigável para URLs", example = "honda-civic-2022-exl")
        String slug,
        @Schema(description = "Título do anúncio", example = "Honda Civic 2.0 EXL Automático")
        String title,
        @Schema(description = "Marca do veículo", example = "Honda")
        String brand,
        @Schema(description = "Modelo do veículo", example = "Civic")
        String model,
        @Schema(description = "Ano de fabricação", example = "2022")
        Integer year,
        @Schema(description = "Ano do modelo", example = "2022")
        Integer modelYear,
        @Schema(description = "Quilometragem rodada", example = "35000")
        Integer mileage,
        @Schema(description = "Tipo de transmissão", example = "AUTOMATIC")
        String transmission,
        @Schema(description = "Tipo de combustível", example = "FLEX")
        String fuelType,
        @Schema(description = "Preço formatado em EUR (€)", example = "139.900,00 €")
        String price,
        @Schema(description = "Preço numérico exato", example = "139900.00")
        String rawPrice,
        @Schema(description = "URL da imagem de capa", example = "/uploads/civic-cover.jpg")
        String coverImageUrl,
        @Schema(description = "Indica se o anúncio está destacado", example = "true")
        boolean featured,
        @Schema(description = "Status do veículo", example = "PUBLISHED")
        String status,
        @Schema(description = "Destaques e opcionais principais", example = "Bancos em couro, Teto solar, Painel digital")
        String highlights
) {
}
