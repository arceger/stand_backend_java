package com.stand.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Imagem da galeria do veículo")
public record ImageResponse(
        @Schema(description = "ID único (UUID) da foto", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        @Schema(description = "URL acessível para visualização", example = "/uploads/civic-1.jpg")
        String imageUrl,
        @Schema(description = "Posição de ordenação na galeria (0, 1, 2...)", example = "0")
        int sortOrder,
        @Schema(description = "Indica se esta foto é a capa principal", example = "true")
        boolean cover
) {
}
