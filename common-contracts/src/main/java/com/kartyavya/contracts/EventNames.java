package com.kartyavya.contracts;

public final class EventNames {
	private EventNames() {
	}

	public static final String USER_REGISTERED = "user.registered", PASSWORD_OTP_REQUESTED = "password.otp.requested",
			PASSWORD_RESET = "password.reset";
	public static final String REPORT_CREATED = "report.created",
			REPORT_PENDING_DEPARTMENT = "report.department.pending", REPORT_PENDING_OFFICER = "report.officer.pending",
			REPORT_ASSIGNED = "report.officer.assigned", REPORT_STATUS_CHANGED = "report.status.changed",
			REPORT_RESOLVED = "report.resolved", CLASSIFICATION_CORRECTED = "classification.corrected";
}
