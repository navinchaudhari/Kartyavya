package com.kartyavya.report.exception;

import feign.FeignException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return body(400, "VALIDATION_FAILED", "Validation failed", request, fields);
    }

    @ExceptionHandler(SecurityException.class)
    ResponseEntity<?> denied(Exception exception, HttpServletRequest request) {
        return body(403, "ACCESS_DENIED", exception.getMessage(), request, null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<?> missing(Exception exception, HttpServletRequest request) {
        return body(404, "NOT_FOUND", exception.getMessage(), request, null);
    }

    @ExceptionHandler({OptimisticLockException.class, OptimisticLockingFailureException.class})
    ResponseEntity<?> conflict(Exception exception, HttpServletRequest request) {
        return body(409, "CONCURRENT_UPDATE", "Complaint was updated by another user. Refresh and try again.", request, null);
    }

    @ExceptionHandler(FeignException.class)
    ResponseEntity<?> downstream(FeignException exception, HttpServletRequest request) {
        String downstreamUrl = exception.request() == null ? "" : exception.request().url();
        if (exception.status() == 503 || downstreamUrl.contains("ai-analytics-service")) {
            return body(
                    503,
                    "AI_CLASSIFICATION_UNAVAILABLE",
                    "Gemini classification is unavailable. Verify the API key and AI Analytics Service configuration.",
                    request,
                    null
            );
        }
        return body(502, "DOWNSTREAM_SERVICE_ERROR", "A required microservice is unavailable.", request, null);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    ResponseEntity<?> bad(Exception exception, HttpServletRequest request) {
        return body(400, "BAD_REQUEST", exception.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> other(Exception exception, HttpServletRequest request) {
        return body(500, "INTERNAL_SERVER_ERROR", exception.getMessage(), request, null);
    }

    private ResponseEntity<?> body(
            int status,
            String error,
            String message,
            HttpServletRequest request,
            Object fields
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", status);
        response.put("error", error);
        response.put("message", message);
        response.put("path", request.getRequestURI());
        response.put("correlationId", Optional.ofNullable(request.getHeader("X-Correlation-Id")).orElse("n/a"));
        if (fields != null) response.put("fieldErrors", fields);
        return ResponseEntity.status(status).body(response);
    }
}
