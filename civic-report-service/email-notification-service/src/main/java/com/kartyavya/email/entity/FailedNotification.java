package com.kartyavya.email.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.kartyavya.email.enums.NotificationType;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Document(collection = "failed_notifications")
public class FailedNotification extends BaseEntity {

	@Id
	private String id;

	@Field("event_id")
	@NotBlank
	private String eventId;

	@Field("error_message")
	private String errorMessage;

	@Field("retry_count")
	private Integer retryCount;

	@Field("payload")
	private String payload;

	private String recipientEmail;

	private String subject;

	private String body;
	
	@Field("notification_type")
	private NotificationType notificationType;
	
	@Field("correlation_id")
	private String correlationId;

}