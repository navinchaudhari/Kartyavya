package com.kartyavya.access.exception;

import java.util.Map;

/**
 * Thrown by {@code PageValidator} when {@code page} or {@code size} query parameters
 * are out of range. Carries a field-specific error map so
 * {@code GlobalExceptionHandler} can populate {@code fieldErrors} correctly.
 *
 * <p>HTTP mapping: 400 VALIDATION_FAILED (handled in GlobalExceptionHandler).
 */
public class PageValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public PageValidationException(Map<String, String> fieldErrors) {
        super("Page/size validation failed: " + fieldErrors);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
