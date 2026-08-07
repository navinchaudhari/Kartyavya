package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Canonical error response format for all HTTP errors (as per member-ownership.md)")
public record ApiErrorResponse(
    @Schema(description = "Timestamp when the error occurred", example = "2026-07-27T10:30:00Z")
    Instant timestamp,
    
    @Schema(description = "HTTP status code", example = "400")
    int status,
    
    @Schema(description = "Error code", example = "VALIDATION_FAILED")
    String error,
    
    @Schema(description = "Human-readable message", example = "Request contains invalid fields")
    String message,
    
    @Schema(description = "Path that caused the error", example = "/api/auth/register")
    String path,
    
    @Schema(description = "Tracing correlation ID", example = "0ec70106-713e-4be2-a85a-802681a44bbc")
    String correlationId,
    
    @Schema(description = "Detailed field errors (null if not a validation error)", example = "{\"email\": \"must be a well-formed email address\"}")
    Object fieldErrors
) {}
