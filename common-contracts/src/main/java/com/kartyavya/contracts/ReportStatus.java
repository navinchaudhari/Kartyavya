package com.kartyavya.contracts;

public enum ReportStatus {
	SUBMITTED, CLASSIFIED, PENDING_DEPARTMENT_SETUP, PENDING_OFFICER_ASSIGNMENT, ASSIGNED, IN_PROGRESS, RESOLVED,
	REJECTED;

	public boolean terminal() {
		return this == RESOLVED || this == REJECTED;
	}

	public String display() {
		return switch (this) {
		case IN_PROGRESS -> "In Progress";
		case RESOLVED -> "Completed";
		case SUBMITTED, CLASSIFIED, PENDING_DEPARTMENT_SETUP, PENDING_OFFICER_ASSIGNMENT, ASSIGNED -> "Pending";
		default -> name().charAt(0) + name().substring(1).toLowerCase().replace('_', ' ');
		};
	}
}
