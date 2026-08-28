package com.stand.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Métricas do dashboard e listagem administrativa de veículos")
public record AdminDashboardResponse(
        @Schema(description = "Total de veículos cadastrados no sistema", example = "15")
        long total,
        @Schema(description = "Total de veículos em destaque na vitrine", example = "4")
        long highlighted,
        @Schema(description = "Total de veículos atualmente publicados", example = "10")
        long published,
        @Schema(description = "Lista de veículos cadastrados")
        List<AdminVehicleResponse> vehicles
) {
}
