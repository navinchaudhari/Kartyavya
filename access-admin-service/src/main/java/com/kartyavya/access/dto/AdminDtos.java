package com.kartyavya.access.dto;

import jakarta.validation.constraints.*;

public final class AdminDtos {
	private AdminDtos() {
	}

	public record DepartmentRequest(@NotBlank @Size(min = 2, max = 120) String departmentName,
			@NotBlank @Email String contactEmail, @Size(max = 255) String description, Boolean enabled) {
	}

	public record DepartmentResponse(Long departmentId, String departmentName, String contactEmail, String description,
			boolean enabled) {
	}

	public record OfficerCreate(@NotBlank @Size(min = 2, max = 120) String fullName, @NotBlank @Email String email,
			@NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$") String password,
			@NotBlank @Pattern(regexp = "^[6-9]\\d{9}$") String mobileNumber, @Size(max = 255) String address,
			@NotNull @Positive Long departmentId) {
	}

	public record OfficerUpdate(@NotBlank @Size(min = 2, max = 120) String fullName,
			@NotBlank @Pattern(regexp = "^[6-9]\\d{9}$") String mobileNumber, @Size(max = 255) String address,
			@NotNull @Positive Long departmentId, Boolean enabled) {
	}

	public record OfficerResponse(Long userId, String fullName, String email, String mobileNumber, String address,
			Long departmentId, String departmentName, boolean enabled) {
	}

	public record RoutingRuleRequest(@NotBlank String category, @NotNull @Positive Long departmentId, Boolean active) {
	}

	public record RoutingRuleResponse(Long id, String category, Long departmentId, String departmentName,
			boolean active) {
	}

	public record UserResponse(Long userId, String fullName, String email, String mobileNumber, String address,
			String role, boolean enabled) {
	}
}
