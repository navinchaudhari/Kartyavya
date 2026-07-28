package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for creating a new officer account")
public record CreateOfficerRequest(

    @NotBlank
    @Schema(description = "Officer full name", example = "Ravi Kumar")
    String name,

    @NotBlank
    @Email
    @Schema(description = "Officer email address (normalized to lowercase before storage)", example = "ravi.kumar@kartyavya.local")
    String email,

    @NotBlank
    @Schema(description = "Initial password (8–72 chars, upper+lower+digit+special)", example = "Secure@123")
    String password,

    @NotNull
    @Schema(description = "ID of the department to assign this officer to", example = "1")
    Long departmentId
) {}
