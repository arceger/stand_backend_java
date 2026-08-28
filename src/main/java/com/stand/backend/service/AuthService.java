package com.stand.backend.service;

import com.stand.backend.dto.AuthResponse;
import com.stand.backend.exception.BadRequestException;
import com.stand.backend.model.AdminSession;
import com.stand.backend.model.AdminUser;
import com.stand.backend.repository.AdminSessionRepository;
import com.stand.backend.repository.AdminUserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final AdminSessionRepository adminSessionRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AdminUserRepository adminUserRepository,
            AdminSessionRepository adminSessionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.adminUserRepository = adminUserRepository;
        this.adminSessionRepository = adminSessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse authenticate(String email, String password) {
        AdminUser adminUser = adminUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("Credenciais invalidas."));

        if (!passwordEncoder.matches(password, adminUser.getPasswordHash())) {
            throw new BadRequestException("Credenciais invalidas.");
        }

        adminSessionRepository.deleteByExpiresAtBefore(Instant.now());
        String token = UUID.randomUUID() + "." + UUID.randomUUID();
        AdminSession session = adminSessionRepository.save(
                new AdminSession(adminUser, token, Instant.now().plus(10, ChronoUnit.DAYS))
        );
        return new AuthResponse(session.getToken(), adminUser.getFullName(), adminUser.getEmail());
    }

    public AdminUser resolveAdmin(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Sessao administrativa invalida.");
        }
        AdminSession session = adminSessionRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Sessao administrativa invalida."));
        if (session.getExpiresAt().isBefore(Instant.now())) {
            adminSessionRepository.delete(session);
            throw new BadRequestException("Sessao administrativa expirada.");
        }
        return session.getAdminUser();
    }
}
