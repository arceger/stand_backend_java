package com.stand.backend;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {
    List<Vehicle> findTop6ByStatusOrderByFeaturedDescCreatedAtDesc(VehicleStatus status);

    Optional<Vehicle> findBySlugAndStatus(String slug, VehicleStatus status);
}


interface VehicleImageRepository extends JpaRepository<VehicleImage, UUID> {
    List<VehicleImage> findByVehicleIdOrderBySortOrderAsc(UUID vehicleId);

    Optional<VehicleImage> findByIdAndVehicleId(UUID id, UUID vehicleId);
}

interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {
    Optional<AdminUser> findByEmailIgnoreCase(String email);
}

interface AdminSessionRepository extends JpaRepository<AdminSession, UUID> {
    Optional<AdminSession> findByToken(String token);

    void deleteByExpiresAtBefore(Instant instant);
}

interface VehicleLeadRepository extends JpaRepository<VehicleLead, UUID> {
}
