package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for creating a new department")
public record DepartmentCreateRequest(

    @NotBlank
    @Schema(description = "Department name", example = "Roads & Infrastructure")
    String name,

    @NotBlank
    @Email
    @Schema(description = "Department contact email", example = "roads@kartyavya.local")
    String contactEmail,

    @Schema(description = "Whether the department is enabled; defaults to true if omitted", example = "true")
    Boolean enabled
) {}
