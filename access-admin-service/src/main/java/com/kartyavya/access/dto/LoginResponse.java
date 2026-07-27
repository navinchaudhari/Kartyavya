package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

// Frozen contract: api-contracts.md §1 POST /api/auth/login response 200.
@Schema(description = "Response containing the JWT and user profile summary upon successful login")
public record LoginResponse(
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    String token, 
    
    @Schema(description = "Token expiration timestamp", example = "2026-07-27T11:30:00Z")
    Instant expiresAt, 
    
    @Schema(description = "Summary of the authenticated user")
    UserSummary user
) {}
