package com.kartyavya.email.service;

public interface IdempotencyService {

	/**
	 * Checks whether an event has already been processed.
	 *
	 * @param eventId Event Identifier
	 * @return true if already processed
	 */
	boolean isProcessed(String eventId);

	/**
	 * Marks an event as processed.
	 *
	 * @param eventId Event Identifier
	 */
	void markProcessed(String eventId);

}