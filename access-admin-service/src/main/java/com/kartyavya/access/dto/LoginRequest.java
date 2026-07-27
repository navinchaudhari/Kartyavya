package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to authenticate and obtain a JWT")
public record LoginRequest(
    @NotBlank
    @Schema(description = "User email address", example = "navin@example.com")
    String email,
    
    @NotBlank
    @Schema(description = "Account password", example = "Password@123")
    String password
) {}
