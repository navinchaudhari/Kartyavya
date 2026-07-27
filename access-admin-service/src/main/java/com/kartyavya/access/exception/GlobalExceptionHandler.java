package com.kartyavya.access.exception;

import com.kartyavya.access.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Produces the canonical error JSON defined in docs/contracts/member-ownership.md for every HTTP error.
 * Shape: { timestamp, status, error, message, path, correlationId, fieldErrors }
 *
 * NOTE: docs/contracts/error-response.md does not exist yet; recommend creating it as a
 * follow-up contract-change PR to give this shape a dedicated home (M1 owns docs/contracts/).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 400 — bean-validation failures on @Valid @RequestBody */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                (first, second) -> first,   // keep first message for duplicate fields
                LinkedHashMap::new
            ));

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            Instant.now(), 400, "VALIDATION_FAILED",
            "Request contains invalid fields",
            request.getRequestURI(),
            MDC.get("correlationId"),
            fieldErrors
        ));
    }

    /** 409 — duplicate email on registration; fieldErrors=null per member-ownership.md */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(
            Instant.now(), 409, "DUPLICATE_RESOURCE",
            ex.getMessage(),
            request.getRequestURI(),
            MDC.get("correlationId"),
            null
        ));
    }

    /** 401 — wrong email or wrong password on login; fieldErrors=null */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(
            Instant.now(), 401, "INVALID_CREDENTIALS",
            ex.getMessage(),
            request.getRequestURI(),
            MDC.get("correlationId"),
            null
        ));
    }

    /** 500 — catch-all for unexpected exceptions */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
            Instant.now(), 500, "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            request.getRequestURI(),
            MDC.get("correlationId"),
            null
        ));
    }
}
