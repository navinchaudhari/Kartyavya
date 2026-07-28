package com.kartyavya.email.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
@Document(collection = "processed_events")
public class ProcessedEvent {

    @Id
    private String id;

    @Indexed(unique = true)   // index on a field in a MongoDB collection.
    @Field("event_id")
    private String eventId;

    @Field("processed_at")
    private LocalDateTime processedAt;

}