package com.stand.backend.dto;

import com.stand.backend.model.FuelType;
import com.stand.backend.model.TransmissionType;
import com.stand.backend.model.VehicleStatus;
import java.math.BigDecimal;

public record VehicleUpsertRequest(
        String title,
        String brand,
        String model,
        String version,
        Integer year,
        Integer modelYear,
        BigDecimal price,
        Integer mileage,
        TransmissionType transmission,
        FuelType fuelType,
        String color,
        Integer doors,
        String description,
        String highlights,
        Boolean featured,
        VehicleStatus status
) {
}
