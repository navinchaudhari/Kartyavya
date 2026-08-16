package com.kartyavya.email.controller;

import com.kartyavya.email.document.NotificationLog;
import com.kartyavya.email.repository.NotificationLogRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
	private final NotificationLogRepository repo;

	public NotificationController(NotificationLogRepository r) {
		repo = r;
	}

	@GetMapping
	List<NotificationLog> list() {
		return repo.findTop200ByOrderByCreatedAtDesc();
	}

	@GetMapping("/failed")
	List<NotificationLog> failed() {
		return repo.findTop200ByOrderByCreatedAtDesc().stream().filter(x -> "FAILED".equals(x.getStatus())).toList();
	}
}
