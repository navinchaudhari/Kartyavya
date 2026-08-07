package com.kartyavya.report.integration.dto;

public record UserContactResponse(
        Long id,
        String name,
        String email,
        boolean enabled
) {
}