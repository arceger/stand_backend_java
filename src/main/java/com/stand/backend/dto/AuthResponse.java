package com.stand.backend.dto;

public record AuthResponse(
        String token,
        String email,
        String name
) {
}
