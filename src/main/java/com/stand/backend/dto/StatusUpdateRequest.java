package com.stand.backend.dto;

import com.stand.backend.model.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para atualização de status de publicação do veículo")
public record StatusUpdateRequest(
        @Schema(description = "Novo status do veículo (DRAFT, PUBLISHED, SOLD, ARCHIVED)", example = "PUBLISHED")
        VehicleStatus status
) {
}