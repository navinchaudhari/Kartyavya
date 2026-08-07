package com.kartyavya.email.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.kartyavya.email.enums.NotificationStatus;
import com.kartyavya.email.enums.NotificationType;

import jakarta.validation.constraints.Email;
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
@Document(collection = "notification_logs")           // Class to document . same as @Entity 
public class NotificationLog extends BaseEntity {

    @Id            // Primary key
    private String id;

    @Indexed(unique = true)          // speeds up queries on a single field and can enforce uniqueness.
    @Field("event_id")            // db field(column) name 
    private String eventId;

    @Field("recipient_email")
    @NotBlank
    @Email
    private String recipientEmail;

    @Field("notification_type")
    private NotificationType notificationType;

    @Field("subject")
    private String subject;

    @Field("body")
    private String body;

    @Field("status")
    private NotificationStatus status;

    @Field("sent_at")
    private LocalDateTime sentAt;

    @Field("correlation_id")
    private String correlationId;

}