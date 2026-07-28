package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Routing rule details response")
public record RoutingRuleResponse(
    @Schema(description = "Routing rule unique ID", example = "1")
    Long id,

    @Schema(description = "Complaint category", example = "POTHOLE")
    String category,

    @Schema(description = "ID of the department this category routes to", example = "1")
    Long departmentId,

    @Schema(description = "Whether this rule is currently active", example = "true")
    boolean active
) {}
