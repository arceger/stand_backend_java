package com.stand.backend.repository;

import com.stand.backend.model.AdminSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AdminSessionRepository extends JpaRepository<AdminSession, UUID> {
    Optional<AdminSession> findByToken(String token);

    void deleteByExpiresAtBefore(Instant instant);
}
