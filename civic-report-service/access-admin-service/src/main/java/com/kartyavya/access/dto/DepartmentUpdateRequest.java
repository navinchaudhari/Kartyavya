package com.kartyavya.access.dto;

import com.kartyavya.access.validation.AtLeastOneFieldPresent;
import io.swagger.v3.oas.annotations.media.Schema;

@AtLeastOneFieldPresent
@Schema(description = "Request body for updating an existing department. At least one field must be non-null.")
public record DepartmentUpdateRequest(

    @Schema(description = "New department name; null means no change", example = "Roads & Infrastructure")
    String name,

    @Schema(description = "New contact email; null means no change", example = "roads@kartyavya.local")
    String contactEmail,

    @Schema(description = "New enabled status; null means no change", example = "false")
    Boolean enabled
) {}
