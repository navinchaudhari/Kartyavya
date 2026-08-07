package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for updating a user's enabled/disabled status")
public record UserStatusRequest(

    @NotNull
    @Schema(description = "New account status: true to enable, false to disable", example = "false")
    Boolean enabled
) {}
