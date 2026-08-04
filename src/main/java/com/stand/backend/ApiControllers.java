package com.stand.backend;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/public")
class PublicApiController {
    private final MarketplaceService marketplaceService;

    PublicApiController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @GetMapping("/vehicles")
    List<VehicleCardResponse> listVehicles(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String brand,
        @RequestParam(required = false) Integer minYear,
        @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return marketplaceService.listPublished(new VehicleFilter(search, brand, minYear, maxPrice, null, false))
            .stream()
            .map(VehicleMapper::card)
            .toList();
    }

    @GetMapping("/vehicles/featured")
    List<VehicleCardResponse> featuredVehicles() {
        return marketplaceService.featuredVehicles().stream().map(VehicleMapper::card).toList();
    }

    @GetMapping("/vehicles/{slug}")
    @Transactional(readOnly = true)
    VehicleDetailResponse vehicleDetail(@PathVariable String slug) {
        return VehicleMapper.detail(marketplaceService.getPublishedBySlug(slug));
    }

    @PostMapping("/vehicles/{slug}/leads")
    ResponseEntity<MessageResponse> createLead(@PathVariable String slug, @RequestBody LeadFormRequest request) {
        marketplaceService.createLead(slug, new LeadRequest(
            request.customerName(),
            request.phone(),
            request.email(),
            request.message()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new MessageResponse("Interesse enviado com sucesso. A loja entrara em contato em breve."));
    }
}

@RestController
@RequestMapping("/api/admin")
class AdminApiController {
    private final AuthService authService;
    private final MarketplaceService marketplaceService;

    AdminApiController(AuthService authService, MarketplaceService marketplaceService) {
        this.authService = authService;
        this.marketplaceService = marketplaceService;
    }

    @PostMapping("/auth/login")
    AuthResponse login(@RequestBody LoginRequest request) {
        return authService.authenticate(request.email(), request.password());
    }

    @GetMapping("/vehicles")
    AdminDashboardResponse listVehicles(@RequestParam(required = false) String search, @RequestParam(required = false) VehicleStatus status) {
        List<Vehicle> vehicles = marketplaceService.listAdmin(new VehicleFilter(search, null, null, null, status, false));
        return new AdminDashboardResponse(
            vehicles.size(),
            vehicles.stream().filter(Vehicle::isFeatured).count(),
            vehicles.stream().filter(vehicle -> vehicle.getStatus() == VehicleStatus.PUBLISHED).count(),
            vehicles.stream().map(VehicleMapper::admin).toList()
        );
    }

    @GetMapping("/vehicles/{id}")
    VehicleDetailResponse getVehicle(@PathVariable UUID id) {
        return VehicleMapper.detail(marketplaceService.getAdminVehicle(id));
    }

    @PostMapping("/vehicles")
    ResponseEntity<VehicleDetailResponse> createVehicle(@RequestBody VehicleFormRequest request) {
        Vehicle vehicle = marketplaceService.createVehicle(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(VehicleMapper.detail(vehicle));
    }

    @PutMapping("/vehicles/{id}")
    VehicleDetailResponse updateVehicle(@PathVariable UUID id, @RequestBody VehicleFormRequest request) {
        return VehicleMapper.detail(marketplaceService.updateVehicle(id, request.toCommand()));
    }

    @PatchMapping("/vehicles/{id}/status")
    VehicleDetailResponse updateStatus(@PathVariable UUID id, @RequestBody StatusUpdateRequest request) {
        return VehicleMapper.detail(marketplaceService.updateStatus(id, request.status()));
    }

    @PostMapping(value = "/vehicles/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<ImageResponse> uploadImages(@PathVariable UUID id, @RequestPart("files") List<MultipartFile> files) {
        return marketplaceService.uploadImages(id, files).stream().map(VehicleMapper::image).toList();
    }

    @PutMapping(value = "/vehicles/{id}/images/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<ImageResponse> replaceImage(
        @PathVariable UUID id,
        @PathVariable UUID imageId,
        @RequestPart("file") MultipartFile file
    ) {
        return marketplaceService.replaceImage(id, imageId, file).stream().map(VehicleMapper::image).toList();
    }

    @PutMapping("/vehicles/{id}/images/order")
    List<ImageResponse> reorderImages(@PathVariable UUID id, @RequestBody ImageOrderRequest request) {
        return marketplaceService.reorderImages(id, request.imageIds()).stream().map(VehicleMapper::image).toList();
    }

    @PatchMapping("/vehicles/{id}/images/{imageId}/cover")
    List<ImageResponse> setCover(@PathVariable UUID id, @PathVariable UUID imageId) {
        return marketplaceService.setCoverImage(id, imageId).stream().map(VehicleMapper::image).toList();
    }

    @DeleteMapping("/vehicles/{id}/images/{imageId}")
    List<ImageResponse> deleteImage(@PathVariable UUID id, @PathVariable UUID imageId) {
        return marketplaceService.deleteImage(id, imageId).stream().map(VehicleMapper::image).toList();
    }
}

record LoginRequest(String email, String password) {
}

record LeadFormRequest(String customerName, String phone, String email, String message) {
}

record StatusUpdateRequest(VehicleStatus status) {
}

record ImageOrderRequest(List<UUID> imageIds) {
}

record MessageResponse(String message) {
}

record AdminDashboardResponse(long total, long highlighted, long published, List<AdminVehicleResponse> vehicles) {
}

record VehicleCardResponse(
    UUID id,
    String slug,
    String title,
    String brand,
    String model,
    Integer year,
    Integer modelYear,
    Integer mileage,
    String transmission,
    String fuelType,
    String price,
    String rawPrice,
    String coverImageUrl,
    boolean featured,
    String status,
    String highlights
) {
}

record VehicleDetailResponse(
    UUID id,
    String slug,
    String title,
    String brand,
    String model,
    String version,
    Integer year,
    Integer modelYear,
    String price,
    String rawPrice,
    Integer mileage,
    String transmission,
    String fuelType,
    String color,
    Integer doors,
    String description,
    String highlights,
    boolean featured,
    String status,
    List<ImageResponse> images
) {
}

record AdminVehicleResponse(
    UUID id,
    String slug,
    String title,
    String price,
    String status,
    boolean featured,
    String coverImageUrl
) {
}

record ImageResponse(UUID id, String imageUrl, int sortOrder, boolean cover) {
}

record VehicleFormRequest(
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
    VehicleUpsertRequest toCommand() {
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

final class VehicleMapper {
    private static final Locale BRAZIL = Locale.forLanguageTag("pt-BR");

    private VehicleMapper() {
    }

    static VehicleCardResponse card(Vehicle vehicle) {
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

    static VehicleDetailResponse detail(Vehicle vehicle) {
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

    static AdminVehicleResponse admin(Vehicle vehicle) {
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

    static ImageResponse image(VehicleImage vehicleImage) {
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
        return NumberFormat.getCurrencyInstance(BRAZIL).format(value);
    }
}
