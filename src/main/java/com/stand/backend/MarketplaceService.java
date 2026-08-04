package com.stand.backend;

import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
class MarketplaceService {
    private final VehicleRepository vehicleRepository;
    private final VehicleImageRepository vehicleImageRepository;
    private final VehicleLeadRepository vehicleLeadRepository;
    private final StorageService storageService;

    MarketplaceService(
        VehicleRepository vehicleRepository,
        VehicleImageRepository vehicleImageRepository,
        VehicleLeadRepository vehicleLeadRepository,
        StorageService storageService
    ) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleImageRepository = vehicleImageRepository;
        this.vehicleLeadRepository = vehicleLeadRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    List<Vehicle> listPublished(VehicleFilter filter) {
        return vehicleRepository.findAll(buildFilter(filter, false), Sort.by(Sort.Order.desc("featured"), Sort.Order.desc("createdAt")));
    }

    @Transactional(readOnly = true)
    List<Vehicle> listAdmin(VehicleFilter filter) {
        return vehicleRepository.findAll(buildFilter(filter, true), Sort.by(Sort.Order.desc("updatedAt")));
    }

    @Transactional(readOnly = true)
    List<Vehicle> featuredVehicles() {
        return vehicleRepository.findTop6ByStatusOrderByFeaturedDescCreatedAtDesc(VehicleStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    Vehicle getPublishedBySlug(String slug) {
        return vehicleRepository.findBySlugAndStatus(slug, VehicleStatus.PUBLISHED)
            .orElseThrow(() -> new NotFoundException("Veiculo nao encontrado."));
    }

    @Transactional(readOnly = true)
    Vehicle getAdminVehicle(UUID id) {
        return vehicleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Veiculo nao encontrado."));
    }

    Vehicle createVehicle(VehicleUpsertRequest request) {
        Vehicle vehicle = new Vehicle();
        applyVehicleFields(vehicle, request);
        return vehicleRepository.save(vehicle);
    }

    Vehicle updateVehicle(UUID vehicleId, VehicleUpsertRequest request) {
        Vehicle vehicle = getAdminVehicle(vehicleId);
        applyVehicleFields(vehicle, request);
        return vehicleRepository.save(vehicle);
    }

    Vehicle updateStatus(UUID vehicleId, VehicleStatus status) {
        Vehicle vehicle = getAdminVehicle(vehicleId);
        vehicle.setStatus(status);
        return vehicleRepository.save(vehicle);
    }

    VehicleLead createLead(String slug, LeadRequest request) {
        Vehicle vehicle = getPublishedBySlug(slug);
        return vehicleLeadRepository.save(new VehicleLead(
            vehicle,
            request.customerName().trim(),
            request.phone().trim(),
            request.email() == null ? null : request.email().trim(),
            request.message() == null ? null : request.message().trim(),
            "SITE"
        ));
    }

    List<VehicleImage> uploadImages(UUID vehicleId, List<MultipartFile> files) {
        Vehicle vehicle = getAdminVehicle(vehicleId);
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("Selecione pelo menos uma imagem.");
        }

        List<VehicleImage> currentImages = vehicleImageRepository.findByVehicleIdOrderBySortOrderAsc(vehicleId);
        int nextOrder = currentImages.size();
        List<VehicleImage> createdImages = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            StorageService.StoredImage storedImage = storageService.save(file, vehicleId);
            VehicleImage image = new VehicleImage(
                vehicle,
                storedImage.publicUrl(),
                nextOrder++,
                currentImages.isEmpty() && createdImages.isEmpty(),
                "UPLOAD",
                storedImage.storageKey()
            );
            createdImages.add(vehicleImageRepository.save(image));
        }
        ensureCoverImage(vehicleId);
        return vehicleImageRepository.findByVehicleIdOrderBySortOrderAsc(vehicleId);
    }

    List<VehicleImage> replaceImage(UUID vehicleId, UUID imageId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Selecione uma imagem para substituicao.");
        }
        VehicleImage image = vehicleImageRepository.findByIdAndVehicleId(imageId, vehicleId)
            .orElseThrow(() -> new NotFoundException("Imagem nao encontrada."));
        StorageService.StoredImage storedImage = storageService.save(file, vehicleId);
        if ("UPLOAD".equalsIgnoreCase(image.getSourceKind())) {
            storageService.deleteIfManaged(image.getStorageKey());
        }
        image.setImageUrl(storedImage.publicUrl());
        image.setStorageKey(storedImage.storageKey());
        vehicleImageRepository.save(image);
        return vehicleImageRepository.findByVehicleIdOrderBySortOrderAsc(vehicleId);
    }

    List<VehicleImage> reorderImages(UUID vehicleId, List<UUID> orderedIds) {
        List<VehicleImage> images = vehicleImageRepository.findByVehicleIdOrderBySortOrderAsc(vehicleId);
        if (orderedIds == null || orderedIds.size() != images.size()) {
            throw new BadRequestException("A ordem enviada nao corresponde a galeria atual.");
        }

        for (int index = 0; index < orderedIds.size(); index++) {
            UUID imageId = orderedIds.get(index);
            VehicleImage image = images.stream()
                .filter(candidate -> candidate.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Ordem de imagens invalida."));
            image.setSortOrder(index);
        }
        vehicleImageRepository.saveAll(images);
        return vehicleImageRepository.findByVehicleIdOrderBySortOrderAsc(vehicleId);
    }

    List<VehicleImage> setCoverImage(UUID vehicleId, UUID imageId) {
        List<VehicleImage> images = vehicleImageRepository.findByVehicleIdOrderBySortOrderAsc(vehicleId);
        if (images.isEmpty()) {
            throw new BadRequestException("O veiculo ainda nao possui imagens.");
        }
        boolean found = false;
        for (VehicleImage image : images) {
            boolean cover = image.getId().equals(imageId);
            image.setCover(cover);
            found = found || cover;
        }
        if (!found) {
            throw new NotFoundException("Imagem nao encontrada.");
        }
        vehicleImageRepository.saveAll(images);
        return vehicleImageRepository.findByVehicleIdOrderBySortOrderAsc(vehicleId);
    }

    List<VehicleImage> deleteImage(UUID vehicleId, UUID imageId) {
        VehicleImage image = vehicleImageRepository.findByIdAndVehicleId(imageId, vehicleId)
            .orElseThrow(() -> new NotFoundException("Imagem nao encontrada."));
        if ("UPLOAD".equalsIgnoreCase(image.getSourceKind())) {
            storageService.deleteIfManaged(image.getStorageKey());
        }
        vehicleImageRepository.delete(image);
        List<VehicleImage> remaining = vehicleImageRepository.findByVehicleIdOrderBySortOrderAsc(vehicleId);
        for (int index = 0; index < remaining.size(); index++) {
            remaining.get(index).setSortOrder(index);
        }
        vehicleImageRepository.saveAll(remaining);
        ensureCoverImage(vehicleId);
        return vehicleImageRepository.findByVehicleIdOrderBySortOrderAsc(vehicleId);
    }

    private void applyVehicleFields(Vehicle vehicle, VehicleUpsertRequest request) {
        vehicle.setBrand(requiredText(request.brand(), "Informe a marca."));
        vehicle.setModel(requiredText(request.model(), "Informe o modelo."));
        vehicle.setVersion(optionalText(request.version()));
        vehicle.setTitle(requiredText(request.title(), "Informe o titulo do veiculo."));
        vehicle.setYear(requiredNumber(request.year(), "Informe o ano."));
        vehicle.setModelYear(requiredNumber(request.modelYear(), "Informe o ano modelo."));
        vehicle.setPrice(requiredPrice(request.price()));
        vehicle.setMileage(requiredNumber(request.mileage(), "Informe a quilometragem."));
        vehicle.setTransmission(request.transmission());
        vehicle.setFuelType(request.fuelType());
        vehicle.setColor(optionalText(request.color()));
        vehicle.setDoors(request.doors());
        vehicle.setDescription(requiredText(request.description(), "Informe a descricao."));
        vehicle.setHighlights(requiredText(request.highlights(), "Informe os destaques do anuncio."));
        vehicle.setFeatured(Boolean.TRUE.equals(request.featured()));
        vehicle.setStatus(request.status() == null ? VehicleStatus.DRAFT : request.status());
        vehicle.setSlug(generateUniqueSlug(vehicle, request));
    }

    private Specification<Vehicle> buildFilter(VehicleFilter filter, boolean includeAllStatuses) {
        return (root, query, builder) -> {
            query.distinct(true);
            root.fetch("images", JoinType.LEFT);
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (!includeAllStatuses) {
                predicates.add(builder.equal(root.get("status"), VehicleStatus.PUBLISHED));
            } else if (filter.status() != null) {
                predicates.add(builder.equal(root.get("status"), filter.status()));
            }

            if (hasText(filter.search())) {
                String like = "%" + filter.search().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                    builder.like(builder.lower(root.get("brand")), like),
                    builder.like(builder.lower(root.get("model")), like),
                    builder.like(builder.lower(root.get("version")), like),
                    builder.like(builder.lower(root.get("title")), like)
                ));
            }
            if (hasText(filter.brand())) {
                predicates.add(builder.equal(builder.lower(root.get("brand")), filter.brand().trim().toLowerCase(Locale.ROOT)));
            }
            if (filter.minYear() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("year"), filter.minYear()));
            }
            if (filter.maxPrice() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }
            if (filter.featuredOnly()) {
                predicates.add(builder.equal(root.get("featured"), true));
            }
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private void ensureCoverImage(UUID vehicleId) {
        List<VehicleImage> images = vehicleImageRepository.findByVehicleIdOrderBySortOrderAsc(vehicleId);
        if (images.isEmpty()) {
            return;
        }
        boolean hasCover = images.stream().anyMatch(VehicleImage::isCover);
        if (!hasCover) {
            images.getFirst().setCover(true);
            vehicleImageRepository.save(images.getFirst());
        }
    }

    private String generateUniqueSlug(Vehicle vehicle, VehicleUpsertRequest request) {
        String baseSlug = slugify("%s-%s-%s-%s".formatted(
            request.brand(),
            request.model(),
            optionalText(request.version()) == null ? "catalogo" : request.version(),
            request.year()
        ));
        String slug = baseSlug;
        int suffix = 2;
        while (slugInUse(slug, vehicle.getId())) {
            slug = baseSlug + "-" + suffix++;
        }
        return slug;
    }

    private boolean slugInUse(String slug, UUID currentId) {
        return vehicleRepository.findAll((root, query, builder) -> builder.equal(root.get("slug"), slug)).stream()
            .anyMatch(vehicle -> !vehicle.getId().equals(currentId));
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    private String requiredText(String value, String message) {
        if (!hasText(value)) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private Integer requiredNumber(Integer value, String message) {
        if (value == null) {
            throw new BadRequestException(message);
        }
        return value;
    }

    private BigDecimal requiredPrice(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Informe um preco valido.");
        }
        return value;
    }

    private String optionalText(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String slugify(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-zA-Z0-9]+", "-")
            .replaceAll("(^-|-$)", "")
            .toLowerCase(Locale.ROOT);
    }
}

record VehicleFilter(
    String search,
    String brand,
    Integer minYear,
    BigDecimal maxPrice,
    VehicleStatus status,
    boolean featuredOnly
) {
}

record VehicleUpsertRequest(
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

record LeadRequest(
    String customerName,
    String phone,
    String email,
    String message
) {
}

class NotFoundException extends RuntimeException {
    NotFoundException(String message) {
        super(message);
    }
}

class BadRequestException extends RuntimeException {
    BadRequestException(String message) {
        super(message);
    }
}
