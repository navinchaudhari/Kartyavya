package com.kartyavya.access.exception;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.kartyavya.access.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

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

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 400 — bean-validation failures on @Valid @RequestBody (field-level AND class-level). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        // Collect field-level errors (e.g. @NotBlank, @Email, @NotNull)
        ex.getBindingResult().getFieldErrors().forEach(fe ->
            fieldErrors.putIfAbsent(
                fe.getField(),
                fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value"
            )
        );

        // Collect class-level (global) errors — e.g. @AtLeastOneFieldPresent.
        // Keyed by literal "request" (not error.getObjectName()) for a predictable, stable contract.
        ex.getBindingResult().getGlobalErrors().forEach(ge ->
            fieldErrors.putIfAbsent(
                "request",
                ge.getDefaultMessage() != null ? ge.getDefaultMessage() : "Invalid request"
            )
        );

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            Instant.now(), 400, "VALIDATION_FAILED",
            "Request contains invalid fields",
            request.getRequestURI(),
            MDC.get("correlationId"),
            fieldErrors
        ));
    }

    /** 400 — method-level validation failures (e.g. @Positive @PathVariable in Spring Boot 3.2+). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        ex.getValueResults().forEach(pvr -> {
            String rawParamName = pvr.getMethodParameter().getParameterName();
            if (rawParamName == null) {
                org.springframework.web.bind.annotation.PathVariable pv = pvr.getMethodParameter().getParameterAnnotation(org.springframework.web.bind.annotation.PathVariable.class);
                if (pv != null && !pv.value().isEmpty()) {
                    rawParamName = pv.value();
                } else if (pv != null && !pv.name().isEmpty()) {
                    rawParamName = pv.name();
                }
            }
            final String paramName = rawParamName != null ? rawParamName : "parameter";
            
            pvr.getResolvableErrors().forEach(re ->
                fieldErrors.putIfAbsent(
                    paramName,
                    re.getDefaultMessage() != null ? re.getDefaultMessage() : "Invalid value"
                )
            );
        });

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            Instant.now(), 400, "VALIDATION_FAILED",
            "Request contains invalid fields",
            request.getRequestURI(),
            MDC.get("correlationId"),
            fieldErrors
        ));
    }

    /**
     * 400 — request body unreadable or contains unknown fields
     * (triggered by {@code spring.jackson.deserialization.fail-on-unknown-properties=true}).
     * Best-effort: extracts offending field name from the cause when available.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = null;
        Throwable cause = ex.getCause();
        if (cause instanceof com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException upe) {
            String fieldName = upe.getPropertyName();
            if (fieldName != null) {
                fieldErrors = Map.of(fieldName, "unknown or unreadable field");
            }
        }

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            Instant.now(), 400, "VALIDATION_FAILED",
            "Request body could not be read or contains unknown fields",
            request.getRequestURI(),
            MDC.get("correlationId"),
            fieldErrors
        ));
    }

    /** 400 — pagination parameters out of range */
    @ExceptionHandler(PageValidationException.class)
    public ResponseEntity<ApiErrorResponse> handlePageValidation(
            PageValidationException ex, HttpServletRequest request) {

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            Instant.now(), 400, "VALIDATION_FAILED",
            "Invalid pagination parameters",
            request.getRequestURI(),
            MDC.get("correlationId"),
            ex.getFieldErrors()
        ));
    }

    /** 400 — invalid routing category */
    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCategory(
            InvalidCategoryException ex, HttpServletRequest request) {

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            Instant.now(), 400, "VALIDATION_FAILED",
            ex.getMessage(),
            request.getRequestURI(),
            MDC.get("correlationId"),
            Map.of("category", "must be one of POTHOLE, GARBAGE, STREETLIGHT, WATER_LEAKAGE, OTHER")
        ));
    }

    /** 400 — write operation targets a disabled department */
    @ExceptionHandler(DepartmentDisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDepartmentDisabled(
            DepartmentDisabledException ex, HttpServletRequest request) {

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            Instant.now(), 400, "INVALID_REQUEST",
            ex.getMessage(),
            request.getRequestURI(),
            MDC.get("correlationId"),
            null
        ));
    }

    /** 400 — ADMIN attempting to disable their own account */
    @ExceptionHandler(SelfActionNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleSelfAction(
            SelfActionNotAllowedException ex, HttpServletRequest request) {

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
            Instant.now(), 400, "INVALID_REQUEST",
            ex.getMessage(),
            request.getRequestURI(),
            MDC.get("correlationId"),
            null
        ));
    }

    /** 404 — entity not found by ID (admin CRUD paths) */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(
            Instant.now(), 404, "RESOURCE_NOT_FOUND",
            ex.getMessage(),
            request.getRequestURI(),
            MDC.get("correlationId"),
            null
        ));
    }

    /** 404 — routing rule not found or not usable (internal feign resolve path) */
    @ExceptionHandler(RoutingRuleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRoutingRuleNotFound(
            RoutingRuleNotFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(
            Instant.now(), 404, "ROUTING_RULE_NOT_FOUND",
            ex.getMessage(),
            request.getRequestURI(),
            MDC.get("correlationId"),
            null
        ));
    }

    /** 409 — duplicate resource (email, department name, category) */
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

    /** 401 — wrong email or wrong password on login */
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

        log.error("Unhandled exception on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
            Instant.now(), 500, "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            request.getRequestURI(),
            MDC.get("correlationId"),
            null
        ));
    }
}
