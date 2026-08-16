package com.kartyavya.ai.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class AiExceptionHandler {
    @ExceptionHandler(GeminiApiException.class)
    ResponseEntity<Map<String, Object>> handleGemini(
            GeminiApiException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "timestamp", Instant.now(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "AI_PROVIDER_UNAVAILABLE",
                "message", exception.getMessage(),
                "path", request.getRequestURI()
        ));
    }
}
