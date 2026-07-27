package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

// Frozen contract: api-contracts.md §1 — name 2..120, email, password 8..72 upper+lower+digit+special.
@Schema(description = "Request to register a new citizen account")
public record RegisterRequest(

    @NotBlank
    @Size(min = 2, max = 120)
    @Schema(description = "Full name of the citizen", example = "Navin Chaudhari")
    String name,

    @NotBlank
    @Email
    @Schema(description = "User email address", example = "navin@example.com")
    String email,

    @NotBlank
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    @Pattern(
        regexp  = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    @Schema(description = "Password containing uppercase, lowercase, number and special character", example = "Password@123")
    String password

) {}
