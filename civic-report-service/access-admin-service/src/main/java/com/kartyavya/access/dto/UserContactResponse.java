package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User contact details for internal routing responses")
public record UserContactResponse(
        @Schema(description = "System user ID", example = "42")
        Long id,
        
        @Schema(description = "Full name", example = "Jane Doe")
        String name,
        
        @Schema(description = "Email address", example = "jane.doe@kartyavya.local")
        String email,
        
        @Schema(description = "Whether the user is currently enabled", example = "true")
        boolean enabled
) {}
