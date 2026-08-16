package com.kartyavya.ai.controller;

import com.kartyavya.ai.service.ClassificationService;
import com.kartyavya.ai.service.GeminiClient;
import com.kartyavya.contracts.ClassificationContracts.Request;
import com.kartyavya.contracts.ClassificationContracts.Response;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
public class AiController {
	private final ClassificationService service;
	private final GeminiClient gemini;

	@Value("${security.internal-key}")
	private String internalKey;

	public AiController(ClassificationService service, GeminiClient gemini) {
		this.service = service;
		this.gemini = gemini;
	}

	@PostMapping("/api/ai/classify")
	Response classify(@Valid @RequestBody Request request) {
		return service.classify(request);
	}

	@GetMapping("/api/ai/status")
	Map<String, Object> status() {
		return Map.of("provider", "Google Gemini Developer API", "configured", gemini.configured(), "modelVersion",
				gemini.modelVersion(), "mode", "STRICT_AI_NO_KEYWORD_FALLBACK");
	}

	@PostMapping("/internal/classify")
	Response internal(@Valid @RequestBody Request request, @RequestHeader("X-Internal-Key") String suppliedKey) {
		if (!secureEquals(internalKey, suppliedKey)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		return service.classify(request);
	}

	private boolean secureEquals(String expected, String supplied) {
		if (expected == null || expected.isBlank() || supplied == null || supplied.isBlank()) {
			return false;
		}
		return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
				supplied.getBytes(StandardCharsets.UTF_8));
	}
}
