package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response from the internal routing resolution endpoint
 * {@code GET /internal/routing/resolve?category={category}}.
 *
 * <p>Shape is frozen in docs/contracts/feign-contracts.md.
 * Used by civic-report-service (M2) to determine which department handles a given category.
 */
@Schema(description = "Internal routing resolution response (feign-contracts.md)")
public record DepartmentRoutingResponse(

    @Schema(description = "The complaint category resolved", example = "POTHOLE")
    String category,

    @Schema(description = "ID of the routed department", example = "1")
    Long departmentId,

    @Schema(description = "Name of the routed department", example = "Roads & Infrastructure")
    String departmentName,

    @Schema(description = "Contact email of the routed department", example = "roads@kartyavya.local")
    String departmentEmail
) {}
