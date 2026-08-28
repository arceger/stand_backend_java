package com.stand.backend.repository;

import com.stand.backend.model.VehicleLead;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface VehicleLeadRepository extends JpaRepository<VehicleLead, UUID> {
}
