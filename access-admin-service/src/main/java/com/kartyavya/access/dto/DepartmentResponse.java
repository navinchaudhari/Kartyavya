package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Department details response")
public record DepartmentResponse(
    @Schema(description = "Department unique ID", example = "1")
    Long id,

    @Schema(description = "Department name", example = "Roads & Infrastructure")
    String name,

    @Schema(description = "Department contact email", example = "roads@kartyavya.local")
    String contactEmail,

    @Schema(description = "Whether the department is currently enabled", example = "true")
    boolean enabled
) {}
