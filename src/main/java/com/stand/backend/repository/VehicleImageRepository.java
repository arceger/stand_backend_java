package com.stand.backend.repository;

import com.stand.backend.model.VehicleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleImageRepository extends JpaRepository<VehicleImage, UUID> {
    List<VehicleImage> findByVehicleIdOrderBySortOrderAsc(UUID vehicleId);

    Optional<VehicleImage> findByIdAndVehicleId(UUID id, UUID vehicleId);
}
