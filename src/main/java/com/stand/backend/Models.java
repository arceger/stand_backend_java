package com.stand.backend;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

enum TransmissionType {
    MANUAL,
    AUTOMATIC
}

enum FuelType {
    FLEX,
    GASOLINE,
    DIESEL,
    HYBRID,
    ELECTRIC
}

enum VehicleStatus {
    DRAFT,
    PUBLISHED,
    SOLD,
    ARCHIVED
}

@Entity
@Table(name = "admin_user")
class AdminUser {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AdminUser() {
    }

    AdminUser(String email, String passwordHash, String fullName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }

    @PrePersist
    void prePersist() {
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    UUID getId() {
        return id;
    }

    String getEmail() {
        return email;
    }

    String getPasswordHash() {
        return passwordHash;
    }

    String getFullName() {
        return fullName;
    }
}

@Entity
@Table(name = "admin_session")
class AdminSession {
    @Id
    @GeneratedValue
    private UUID id;

   // @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.EAGER) // Em vez de LAZY
    @JoinColumn(name = "admin_user_id", nullable = false)
    private AdminUser adminUser;

    @Column(nullable = false, unique = true, length = 120)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AdminSession() {
    }

    AdminSession(AdminUser adminUser, String token, Instant expiresAt) {
        this.adminUser = adminUser;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void prePersist() {
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    AdminUser getAdminUser() {
        return adminUser;
    }

    String getToken() {
        return token;
    }

    Instant getExpiresAt() {
        return expiresAt;
    }
}

@Entity
@Table(name = "vehicle")
class Vehicle {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(nullable = false, length = 120)
    private String brand;

    @Column(nullable = false, length = 120)
    private String model;

    @Column(length = 160)
    private String version;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "model_year", nullable = false)
    private Integer modelYear;

    @Column(name = "price_value", nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer mileage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransmissionType transmission;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 30)
    private FuelType fuelType;

    @Column(length = 80)
    private String color;

    private Integer doors;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false, columnDefinition = "text")
    private String highlights;

    @Column(nullable = false)
    private boolean featured;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc")
    private final List<VehicleImage> images = new ArrayList<>();

    protected Vehicle() {
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = createdAt == null ? now : createdAt;
        updatedAt = updatedAt == null ? now : updatedAt;
        status = status == null ? VehicleStatus.DRAFT : status;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    UUID getId() {
        return id;
    }

    String getSlug() {
        return slug;
    }

    void setSlug(String slug) {
        this.slug = slug;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getBrand() {
        return brand;
    }

    void setBrand(String brand) {
        this.brand = brand;
    }

    String getModel() {
        return model;
    }

    void setModel(String model) {
        this.model = model;
    }

    String getVersion() {
        return version;
    }

    void setVersion(String version) {
        this.version = version;
    }

    Integer getYear() {
        return year;
    }

    void setYear(Integer year) {
        this.year = year;
    }

    Integer getModelYear() {
        return modelYear;
    }

    void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    BigDecimal getPrice() {
        return price;
    }

    void setPrice(BigDecimal price) {
        this.price = price;
    }

    Integer getMileage() {
        return mileage;
    }

    void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    TransmissionType getTransmission() {
        return transmission;
    }

    void setTransmission(TransmissionType transmission) {
        this.transmission = transmission;
    }

    FuelType getFuelType() {
        return fuelType;
    }

    void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    String getColor() {
        return color;
    }

    void setColor(String color) {
        this.color = color;
    }

    Integer getDoors() {
        return doors;
    }

    void setDoors(Integer doors) {
        this.doors = doors;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    String getHighlights() {
        return highlights;
    }

    void setHighlights(String highlights) {
        this.highlights = highlights;
    }

    boolean isFeatured() {
        return featured;
    }

    void setFeatured(boolean featured) {
        this.featured = featured;
    }

    VehicleStatus getStatus() {
        return status;
    }

    void setStatus(VehicleStatus status) {
        this.status = status;
    }

    List<VehicleImage> getImages() {
        return images;
    }
}

@Entity
@Table(name = "vehicle_image")
class VehicleImage {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "image_url", nullable = false, columnDefinition = "text")
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_cover", nullable = false)
    private boolean cover;

    @Column(name = "source_kind", nullable = false, length = 20)
    private String sourceKind;

    @Column(name = "storage_key", length = 255)
    private String storageKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected VehicleImage() {
    }

    VehicleImage(Vehicle vehicle, String imageUrl, Integer sortOrder, boolean cover, String sourceKind, String storageKey) {
        this.vehicle = vehicle;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        this.cover = cover;
        this.sourceKind = sourceKind;
        this.storageKey = storageKey;
    }

    @PrePersist
    void prePersist() {
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    UUID getId() {
        return id;
    }

    Vehicle getVehicle() {
        return vehicle;
    }

    String getImageUrl() {
        return imageUrl;
    }

    void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    Integer getSortOrder() {
        return sortOrder;
    }

    void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    boolean isCover() {
        return cover;
    }

    void setCover(boolean cover) {
        this.cover = cover;
    }

    String getSourceKind() {
        return sourceKind;
    }

    String getStorageKey() {
        return storageKey;
    }

    void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }
}

@Entity
@Table(name = "vehicle_lead")
class VehicleLead {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "customer_name", nullable = false, length = 160)
    private String customerName;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(length = 160)
    private String email;

    @Column(columnDefinition = "text")
    private String message;

    @Column(nullable = false, length = 40)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected VehicleLead() {
    }

    VehicleLead(Vehicle vehicle, String customerName, String phone, String email, String message, String source) {
        this.vehicle = vehicle;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.message = message;
        this.source = source;
    }

    @PrePersist
    void prePersist() {
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
