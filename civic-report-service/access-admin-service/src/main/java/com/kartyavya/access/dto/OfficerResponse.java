package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Officer details response (used by GET /api/admin/officers)")
public record OfficerResponse(
    @Schema(description = "User (officer) unique ID", example = "5")
    Long id,

    @Schema(description = "Officer full name", example = "Ravi Kumar")
    String name,

    @Schema(description = "Officer email address", example = "ravi.kumar@kartyavya.local")
    String email,

    @Schema(description = "ID of the assigned department", example = "1")
    Long departmentId,

    @Schema(description = "Name of the assigned department", example = "Roads & Infrastructure")
    String departmentName
) {}
