package com.kartyavya.access.dto;

import com.kartyavya.access.validation.AtLeastOneFieldPresent;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Routing-rule partial update. Category is intentionally absent — it is immutable after creation.
 * Sending {@code category} in the request body returns VALIDATION_FAILED/400 because
 * {@code spring.jackson.deserialization.fail-on-unknown-properties=true} is active.
 */
@AtLeastOneFieldPresent
@Schema(description = "Request body for updating a routing rule. At least one field must be non-null. " +
        "category is immutable — supplying it returns 400.")
public record RoutingRuleUpdateRequest(

    @Schema(description = "New target department ID; null means no change", example = "2")
    Long departmentId,

    @Schema(description = "New active status; null means no change", example = "false")
    Boolean active
) {}
