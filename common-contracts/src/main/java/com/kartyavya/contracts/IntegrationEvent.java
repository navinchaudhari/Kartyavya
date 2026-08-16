package com.kartyavya.contracts;

import java.time.Instant;
import java.util.UUID;

public record IntegrationEvent<T>(UUID eventId, String eventType, String correlationId, Instant occurredAt, T data) {
	public static <T> IntegrationEvent<T> of(String type, String correlationId, T data) {
		return new IntegrationEvent<>(UUID.randomUUID(), type, correlationId, Instant.now(), data);
	}
}
