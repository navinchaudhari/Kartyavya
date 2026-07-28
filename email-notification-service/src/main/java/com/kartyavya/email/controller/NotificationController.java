package com.kartyavya.email.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kartyavya.email.dto.response.NotificationResponse;
import com.kartyavya.email.service.NotificationLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "Email Notification Management APIs")
public class NotificationController {

	private final NotificationLogService notificationLogService;

	@Operation(summary = "Get all notifications")
	@GetMapping
	public ResponseEntity<List<NotificationResponse>> getAllNotifications() {

		return ResponseEntity.ok(notificationLogService.getAllNotifications());
	}

	@Operation(summary = "Get notifications by recipient email")
	@GetMapping("/mine")
	public ResponseEntity<List<NotificationResponse>> getMyNotifications(@RequestParam String email) {

		return ResponseEntity.ok(notificationLogService.getNotificationsByRecipientEmail(email));
	}

	@Operation(summary = "Get notification by id")
	@GetMapping("/{id}")
	public ResponseEntity<NotificationResponse> getNotification(@PathVariable(value = "id") String id) {

	    return ResponseEntity.ok(notificationLogService.getNotificationById(id));
	}

	@Operation(summary = "Retry failed notification")
	@PostMapping("/{id}/retry")
	public ResponseEntity<String> retryNotification(@PathVariable String id) {

		notificationLogService.retryNotification(id);

		return ResponseEntity.ok("Notification retried successfully.");
	}
	
	

}