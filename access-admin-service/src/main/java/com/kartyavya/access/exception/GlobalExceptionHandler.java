package com.kartyavya.access.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<?> validation(MethodArgumentNotValidException e, HttpServletRequest r) {
		Map<String, String> f = new LinkedHashMap<>();
		e.getBindingResult().getFieldErrors().forEach(x -> f.putIfAbsent(x.getField(), x.getDefaultMessage()));
		return body(400, "VALIDATION_FAILED", "Validation failed", r, f);
	}

	@ExceptionHandler(SecurityException.class)
	ResponseEntity<?> auth(SecurityException e, HttpServletRequest r) {
		return body(401, "INVALID_CREDENTIALS", e.getMessage(), r, null);
	}

	@ExceptionHandler(NoSuchElementException.class)
	ResponseEntity<?> missing(Exception e, HttpServletRequest r) {
		return body(404, "NOT_FOUND", e.getMessage(), r, null);
	}

	@ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
	ResponseEntity<?> bad(Exception e, HttpServletRequest r) {
		return body(400, "BAD_REQUEST", e.getMessage(), r, null);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<?> other(Exception e, HttpServletRequest r) {
		return body(500, "INTERNAL_SERVER_ERROR", e.getMessage(), r, null);
	}

	private ResponseEntity<?> body(int s, String err, String msg, HttpServletRequest r, Object fields) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("timestamp", Instant.now());
		m.put("status", s);
		m.put("error", err);
		m.put("message", msg);
		m.put("path", r.getRequestURI());
		m.put("correlationId", Optional.ofNullable(r.getHeader("X-Correlation-Id")).orElse("n/a"));
		if (fields != null)
			m.put("fieldErrors", fields);
		return ResponseEntity.status(s).body(m);
	}
}
