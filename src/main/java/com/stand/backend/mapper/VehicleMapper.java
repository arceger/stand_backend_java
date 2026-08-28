package com.stand.backend.mapper;

import com.stand.backend.dto.*;
import com.stand.backend.model.Vehicle;
import com.stand.backend.model.VehicleImage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class VehicleMapper {
    private static final Locale PORTUGAL = Locale.forLanguageTag("pt-PT");

    private VehicleMapper() {
    }

    public static VehicleCardResponse card(Vehicle vehicle) {
        return new VehicleCardResponse(
                vehicle.getId(),
                vehicle.getSlug(),
                vehicle.getTitle(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getModelYear(),
                vehicle.getMileage(),
                vehicle.getTransmission().name(),
                vehicle.getFuelType().name(),
                currency(vehicle.getPrice()),
                vehicle.getPrice().toPlainString(),
                coverUrl(vehicle),
                vehicle.isFeatured(),
                vehicle.getStatus().name(),
                vehicle.getHighlights()
        );
    }

    public static VehicleDetailResponse detail(Vehicle vehicle) {
        List<ImageResponse> images = vehicle.getImages().stream()
                .sorted(Comparator.comparing(VehicleImage::getSortOrder))
                .map(VehicleMapper::image)
                .toList();
        return new VehicleDetailResponse(
                vehicle.getId(),
                vehicle.getSlug(),
                vehicle.getTitle(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getVersion(),
                vehicle.getYear(),
                vehicle.getModelYear(),
                currency(vehicle.getPrice()),
                vehicle.getPrice().toPlainString(),
                vehicle.getMileage(),
                vehicle.getTransmission().name(),
                vehicle.getFuelType().name(),
                vehicle.getColor(),
                vehicle.getDoors(),
                vehicle.getDescription(),
                vehicle.getHighlights(),
                vehicle.isFeatured(),
                vehicle.getStatus().name(),
                images
        );
    }

    public static AdminVehicleResponse admin(Vehicle vehicle) {
        return new AdminVehicleResponse(
                vehicle.getId(),
                vehicle.getSlug(),
                vehicle.getTitle(),
                currency(vehicle.getPrice()),
                vehicle.getStatus().name(),
                vehicle.isFeatured(),
                coverUrl(vehicle)
        );
    }

    public static ImageResponse image(VehicleImage vehicleImage) {
        return new ImageResponse(vehicleImage.getId(), vehicleImage.getImageUrl(), vehicleImage.getSortOrder(), vehicleImage.isCover());
    }

    private static String coverUrl(Vehicle vehicle) {
        return vehicle.getImages().stream()
                .filter(VehicleImage::isCover)
                .findFirst()
                .or(() -> vehicle.getImages().stream().findFirst())
                .map(VehicleImage::getImageUrl)
                .orElse("");
    }

    private static String currency(BigDecimal value) {
        if (value == null) {
            return "0,00 €";
        }
        return NumberFormat.getCurrencyInstance(PORTUGAL).format(value);
    }
}
