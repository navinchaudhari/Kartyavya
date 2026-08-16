package com.kartyavya.email.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("processed_events")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedEvent {
	@Id
	private String eventId;
	private String eventType;
	private Instant processedAt = Instant.now();

	public ProcessedEvent(String i, String t) {
		eventId = i;
		eventType = t;
	}
}
