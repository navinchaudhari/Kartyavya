package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for creating a new routing rule")
public record RoutingRuleCreateRequest(

    @NotBlank
    @Schema(description = "Complaint category (frozen enum)",
            example = "POTHOLE",
            allowableValues = {"POTHOLE", "GARBAGE", "STREETLIGHT", "WATER_LEAKAGE", "OTHER"})
    String category,

    @NotNull
    @Schema(description = "ID of the department this category routes to", example = "1")
    Long departmentId,

    @Schema(description = "Whether this rule is active; defaults to true if omitted", example = "true")
    Boolean active
) {}
