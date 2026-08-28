package com.stand.backend.dto;

import com.stand.backend.model.FuelType;
import com.stand.backend.model.TransmissionType;
import com.stand.backend.model.VehicleStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Formulário para cadastro ou atualização de veículo")
public record VehicleFormRequest(
        @Schema(description = "Título descritivo do anúncio", example = "Honda Civic 2.0 EXL Automático", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(description = "Marca fabricante", example = "Honda", requiredMode = Schema.RequiredMode.REQUIRED)
        String brand,
        @Schema(description = "Modelo do veículo", example = "Civic", requiredMode = Schema.RequiredMode.REQUIRED)
        String model,
        @Schema(description = "Versão do veículo", example = "2.0 16V EXL CVT")
        String version,
        @Schema(description = "Ano de fabricação", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer year,
        @Schema(description = "Ano do modelo", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer modelYear,
        @Schema(description = "Preço de venda em euros", example = "139900.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal price,
        @Schema(description = "Quilometragem atual do veículo", example = "35000", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer mileage,
        @Schema(description = "Tipo de transmissão / câmbio", example = "AUTOMATIC", requiredMode = Schema.RequiredMode.REQUIRED)
        TransmissionType transmission,
        @Schema(description = "Tipo de combustível", example = "FLEX", requiredMode = Schema.RequiredMode.REQUIRED)
        FuelType fuelType,
        @Schema(description = "Cor do veículo", example = "Prata")
        String color,
        @Schema(description = "Quantidade de portas", example = "4")
        Integer doors,
        @Schema(description = "Descrição completa do veículo", example = "Veículo impecável, único dono, com todas as revisões feitas na concessionária autorizada.", requiredMode = Schema.RequiredMode.REQUIRED)
        String description,
        @Schema(description = "Destaques e opcionais", example = "Bancos em couro, Teto solar, Painel digital")
        String highlights,
        @Schema(description = "Marcar anúncio como destaque", example = "false")
        Boolean featured,
        @Schema(description = "Status inicial de publicação", example = "PUBLISHED")
        VehicleStatus status
) {
    public VehicleUpsertRequest toCommand() {
        return new VehicleUpsertRequest(
                title,
                brand,
                model,
                version,
                year,
                modelYear,
                price,
                mileage,
                transmission,
                fuelType,
                color,
                doors,
                description,
                highlights,
                featured,
                status
        );
    }
}
