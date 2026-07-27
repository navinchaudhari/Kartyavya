package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

// Frozen contract: api-contracts.md §1 POST /api/auth/register response 201.
@Schema(description = "Response containing basic user details")
public record UserResponse(
    @Schema(description = "User unique ID", example = "1")
    Long id,
    
    @Schema(description = "Full name", example = "Navin Chaudhari")
    String name,
    
    @Schema(description = "User email address", example = "navin@example.com")
    String email,
    
    @Schema(description = "Assigned system role", example = "CITIZEN")
    String role,
    
    @Schema(description = "Account status", example = "true")
    boolean enabled,
    
    @Schema(description = "Account creation timestamp", example = "2026-07-27T10:30:00Z")
    Instant createdAt
) {}
