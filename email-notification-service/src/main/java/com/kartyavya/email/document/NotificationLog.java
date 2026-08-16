package com.kartyavya.email.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("notification_logs")
@Getter
@Setter
@NoArgsConstructor
public class NotificationLog {
	@Id
	private String id;
	private String eventId;
	private String recipient;
	private String subject;
	private String type;
	private String status;
	private String error;
	private String htmlBody;
	private Instant createdAt = Instant.now();
	private Instant sentAt;
}
