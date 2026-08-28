package com.stand.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Requisição para reordenação de imagens na galeria")
public record ImageOrderRequest(
        @Schema(description = "Lista ordenada contendo os UUIDs das imagens")
        List<UUID> imageIds
) {
}
