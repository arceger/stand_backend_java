package com.stand.backend;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class AuthService {
    private final AdminUserRepository adminUserRepository;
    private final AdminSessionRepository adminSessionRepository;
    private final PasswordEncoder passwordEncoder;

    AuthService(
        AdminUserRepository adminUserRepository,
        AdminSessionRepository adminSessionRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.adminUserRepository = adminUserRepository;
        this.adminSessionRepository = adminSessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    AuthResponse authenticate(String email, String password) {
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

    AdminUser resolveAdmin(String token) {
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

record AuthResponse(String token, String fullName, String email) {
}
