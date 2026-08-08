package com.kartyavya.email.dto.response;

import java.time.LocalDateTime;

import com.kartyavya.email.enums.NotificationStatus;
import com.kartyavya.email.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

	private String id;

	private String eventId;

	private String recipientEmail;

	private NotificationType notificationType;

	private NotificationStatus status;

	private String subject;

	private String body;

	private String correlationId;

	private LocalDateTime sentAt;

	private LocalDateTime createdAt;

}