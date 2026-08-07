package com.kartyavya.access.controller;

import com.kartyavya.access.dto.AuthDtos.*;
import com.kartyavya.access.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService service;

	public AuthController(AuthService s) {
		service = s;
	}

	private String correlation(String h) {
		return h == null ? java.util.UUID.randomUUID().toString() : h;
	}

	@PostMapping("/register")
	ResponseEntity<?> register(@Valid @RequestBody RegisterRequest r,
			@RequestHeader(value = "X-Correlation-Id", required = false) String c) {
		service.register(r, correlation(c));
		return ResponseEntity.status(201).body(java.util.Map.of("message", "Registration successful"));
	}

	@PostMapping("/login")
	LoginResponse login(@Valid @RequestBody LoginRequest r) {
		return service.login(r);
	}

	@PostMapping("/forgot-password")
	Object forgot(@Valid @RequestBody ForgotRequest r,
			@RequestHeader(value = "X-Correlation-Id", required = false) String c) {
		service.forgot(r, correlation(c));
		return java.util.Map.of("message", "If the email is registered, an OTP has been sent");
	}

	@PostMapping("/verify-otp")
	Object verify(@Valid @RequestBody VerifyOtpRequest r) {
		if (!service.verify(r))
			throw new IllegalArgumentException("OTP is invalid or expired");
		return java.util.Map.of("message", "OTP verified");
	}

	@PostMapping("/reset-password")
	Object reset(@Valid @RequestBody ResetPasswordRequest r,
			@RequestHeader(value = "X-Correlation-Id", required = false) String c) {
		service.reset(r, correlation(c));
		return java.util.Map.of("message", "Password reset successful");
	}
}
