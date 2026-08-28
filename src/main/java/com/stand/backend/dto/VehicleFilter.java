package com.stand.backend.dto;

import com.stand.backend.model.VehicleStatus;
import java.math.BigDecimal;

public record VehicleFilter(
        String search,
        String brand,
        Integer minYear,
        BigDecimal maxPrice,
        VehicleStatus status,
        boolean featuredOnly
) {
}