package com.kartyavya.access.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public final class AuthDtos {
	private AuthDtos() {
	}

	public record RegisterRequest(@NotBlank @Size(min = 2, max = 120) String fullName, @NotBlank @Email String email,
			@NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,14}$", message = "Password must contain uppercase, lowercase, digit and special character") String password,
			@NotBlank @Pattern(regexp = "^[6-9]\\d{9}$") String mobileNumber,
			@NotBlank @Size(min = 5, max = 255) String address) {
	}

	public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
	}

	public record LoginResponse(String token, Instant expiresAt, Long userId, String fullName, String email,
			String role, Long departmentId, String departmentName) {
	}

	public record ForgotRequest(@NotBlank @Email String email) {
	}

	public record VerifyOtpRequest(@NotBlank @Email String email, @NotBlank @Pattern(regexp = "^\\d{6}$") String otp) {
	}

	public record ResetPasswordRequest(@NotBlank @Email String email,
			@NotBlank @Pattern(regexp = "^\\d{6}$") String otp,
			@NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$") String newPassword) {
	}

	public record ProfileUpdate(@NotBlank @Size(min = 2, max = 120) String fullName,
			@NotBlank @Pattern(regexp = "^[6-9]\\d{9}$") String mobileNumber,
			@NotBlank @Size(min = 5, max = 255) String address) {
	}
}
