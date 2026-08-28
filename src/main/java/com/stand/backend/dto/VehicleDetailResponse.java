package com.stand.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Ficha técnica completa e galeria de fotos do veículo")
public record VehicleDetailResponse(
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
        @Schema(description = "Versão específica", example = "2.0 16V EXL CVT")
        String version,
        @Schema(description = "Ano de fabricação", example = "2022")
        Integer year,
        @Schema(description = "Ano do modelo", example = "2022")
        Integer modelYear,
        @Schema(description = "Preço formatado em EUR (€)", example = "139.900,00 €")
        String price,
        @Schema(description = "Preço numérico exato", example = "139900.00")
        String rawPrice,
        @Schema(description = "Quilometragem rodada", example = "35000")
        Integer mileage,
        @Schema(description = "Tipo de transmissão", example = "AUTOMATIC")
        String transmission,
        @Schema(description = "Tipo de combustível", example = "FLEX")
        String fuelType,
        @Schema(description = "Cor do veículo", example = "Prata")
        String color,
        @Schema(description = "Quantidade de portas", example = "4")
        Integer doors,
        @Schema(description = "Descrição detalhada do veículo", example = "Veículo de único dono, revisões em dia na concessionária.")
        String description,
        @Schema(description = "Destaques e opcionais", example = "Bancos em couro, Teto solar, Painel digital")
        String highlights,
        @Schema(description = "Indica se está destacado", example = "true")
        boolean featured,
        @Schema(description = "Status atual do veículo", example = "PUBLISHED")
        String status,
        @Schema(description = "Galeria de imagens do veículo")
        List<ImageResponse> images
) {
}
