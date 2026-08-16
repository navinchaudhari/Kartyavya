package com.kartyavya.contracts;

import java.time.Instant;

public final class Events {
	private Events() {
	}

	public record UserRegistered(Long userId, String fullName, String email) {
	}

	public record PasswordOtpRequested(String fullName, String email, String otp, Instant expiresAt) {
	}

	public record PasswordReset(String fullName, String email) {
	}

	public record ReportCreated(Long reportId, String trackingCode, Long citizenId, String citizenName,
			String citizenEmail, String title, String areaLocation, double latitude, double longitude, String category,
			String severity, double confidence, Long departmentId, String departmentName, Long officerId,
			String officerName, String officerEmail, String status, Instant createdAt) {
	}

	public record ReportPending(Long reportId, String trackingCode, String citizenName, String citizenEmail,
			String title, String category, String pendingReason, String status) {
	}

	public record ReportAssigned(Long reportId, String trackingCode, String citizenName, String citizenEmail,
			String officerName, String officerEmail, String departmentName, String title, String areaLocation,
			String severity) {
	}

	public record ReportStatusChanged(Long reportId, String trackingCode, String citizenName, String citizenEmail,
			String officerName, String previousStatus, String currentStatus, String remarks) {
	}

	public record ReportResolved(Long reportId, String trackingCode, String citizenName, String citizenEmail,
			String officerName, String resolutionRemark, Instant resolvedAt) {
	}

	public record ClassificationCorrected(Long reportId, String originalCategory, String correctedCategory,
			String originalSeverity, String correctedSeverity, Long officerId) {
	}
}
