package com.kartyavya.email.service;

import com.kartyavya.email.enums.NotificationType;

public interface RetryService {

	/**
	 * Stores a failed notification.
	 *
	 * @param eventId      Event Identifier
	 * @param errorMessage Exception message
	 * @param payload      Original payload
	 */
	void saveFailure(String eventId, String errorMessage, String payload, String recipientEmail, String subject,
			String body, NotificationType notificationType);

	/**
	 * Increment retry count.
	 *
	 * @param eventId Event Identifier
	 */
	void incrementRetryCount(String eventId);

}