package com.kartyavya.access.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Standard page response wrapper for paginated endpoints.
 * Shape documented in api-contracts.md §Standard page response.
 *
 * @param <T> the content element type
 */
@Schema(description = "Standard paginated response wrapper")
public record PageResponse<T>(

    @Schema(description = "Page content items")
    List<T> content,

    @Schema(description = "0-based page index returned", example = "0")
    int page,

    @Schema(description = "Page size used for this result", example = "20")
    int size,

    @Schema(description = "Total number of elements across all pages", example = "42")
    long totalElements,

    @Schema(description = "Total number of pages", example = "3")
    int totalPages,

    @Schema(description = "Whether this is the last page", example = "false")
    boolean last
) {}
