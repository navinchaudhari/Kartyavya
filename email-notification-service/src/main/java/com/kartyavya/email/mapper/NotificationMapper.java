package com.kartyavya.email.mapper;

import org.springframework.stereotype.Component;

import com.kartyavya.email.dto.response.NotificationResponse;
import com.kartyavya.email.entity.NotificationLog;

@Component
public class NotificationMapper {

	public NotificationResponse toResponse(NotificationLog entity) {

		if (entity == null) {
			return null;
		}

		return NotificationResponse.builder().id(entity.getId()).eventId(entity.getEventId())
				.recipientEmail(entity.getRecipientEmail()).notificationType(entity.getNotificationType())
				.status(entity.getStatus()).subject(entity.getSubject()).body(entity.getBody())
				.correlationId(entity.getCorrelationId()).sentAt(entity.getSentAt()).createdAt(entity.getCreatedAt())
				.build();
	}

}