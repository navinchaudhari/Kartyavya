package com.kartyavya.contracts;

import java.util.List;

public final class AccessContracts {
	private AccessContracts() {
	}

	public record UserContact(Long userId, String fullName, String email, String mobileNumber, String role,
			Long departmentId, String departmentName, boolean enabled) {
	}

	public record DepartmentInfo(Long departmentId, String departmentName, String contactEmail, boolean enabled) {
	}

	public record OfficerInfo(Long officerId, String fullName, String email, String mobileNumber, Long departmentId,
			String departmentName, boolean enabled) {
	}

	public record RoutingResolution(boolean mapped, String category, Long departmentId, String departmentName,
			String departmentEmail, String reason) {
	}

	public record OfficersResponse(List<OfficerInfo> officers) {
	}
}
