package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// Frozen contract: api-contracts.md §1 LoginResponse.user shape.
// departmentId is null for CITIZEN and ADMIN roles.
@Schema(description = "Summary of user details included in login response")
public record UserSummary(
    @Schema(description = "User unique ID", example = "1")
    Long id, 
    
    @Schema(description = "Full name", example = "Navin Chaudhari")
    String name, 
    
    @Schema(description = "User email address", example = "navin@example.com")
    String email, 
    
    @Schema(description = "Assigned system role", example = "CITIZEN")
    String role, 
    
    @Schema(description = "Department ID if assigned, otherwise null", example = "null")
    Long departmentId
) {}
