package com.kartyavya.access.util;

import com.kartyavya.access.exception.PageValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Validates pagination query parameters against the frozen contract rules:
 * {@code page >= 0}, {@code size} in [1, 100].
 *
 * <p>Throws {@link PageValidationException} with per-field error messages so
 * {@code GlobalExceptionHandler} can return VALIDATION_FAILED/400 with {@code fieldErrors}.
 *
 * <p>Owner: M1.
 */
public final class PageValidator {

    private PageValidator() {}

    /**
     * Validates {@code page} and {@code size}.
     *
     * @param page 0-based page index
     * @param size page size; must be 1–100
     * @throws PageValidationException if either value is out of range
     */
    public static void validate(int page, int size) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (page < 0) {
            errors.put("page", "must be >= 0");
        }
        if (size < 1 || size > 100) {
            errors.put("size", "must be between 1 and 100");
        }
        if (!errors.isEmpty()) {
            throw new PageValidationException(errors);
        }
    }
}
