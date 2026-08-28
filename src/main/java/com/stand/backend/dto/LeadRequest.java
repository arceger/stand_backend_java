package com.stand.backend.dto;

public record LeadRequest(
        String customerName,
        String phone,
        String email,
        String message
) {
}
