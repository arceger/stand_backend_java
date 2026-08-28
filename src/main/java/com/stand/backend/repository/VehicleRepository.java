package com.stand.backend.repository;

import com.stand.backend.model.Vehicle;
import com.stand.backend.model.VehicleStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {

    @EntityGraph(attributePaths = {"images"})
    List<Vehicle> findTop6ByStatusOrderByFeaturedDescCreatedAtDesc(VehicleStatus status);

    @EntityGraph(attributePaths = {"images"})
    Optional<Vehicle> findBySlugAndStatus(String slug, VehicleStatus status);

    @Override
    @EntityGraph(attributePaths = {"images"})
    Optional<Vehicle> findById(UUID id);
}
