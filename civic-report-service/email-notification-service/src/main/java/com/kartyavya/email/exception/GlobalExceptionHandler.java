package com.kartyavya.email.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotificationNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotificationNotFound(NotificationNotFoundException ex) {

		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(TemplateNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleTemplateNotFound(TemplateNotFoundException ex) {

		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(DuplicateEventException.class)
	public ResponseEntity<Map<String, Object>> handleDuplicateEvent(DuplicateEventException ex) {

		return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(EmailSendingException.class)
	public ResponseEntity<Map<String, Object>> handleEmailException(EmailSendingException ex) {

		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	@ExceptionHandler(NotificationException.class)
	public ResponseEntity<Map<String, Object>> handleNotificationException(NotificationException ex) {

		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleException(Exception ex) {

		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {

		Map<String, Object> response = new LinkedHashMap<>();

		response.put("timestamp", LocalDateTime.now());

		response.put("status", status.value());

		response.put("error", status.getReasonPhrase());

		response.put("message", message);

		return new ResponseEntity<>(response, status);
	}

}