package com.kartyavya.email.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.kartyavya.email.entity.NotificationLog;
import com.kartyavya.email.enums.NotificationStatus;

/*
 * Access the notification_logs collection.(Store and query sent notification logs)	
 *  - Save notification logs
	- View notification history
	- Retrieve notifications for REST APIs
	- Retry operations
*/
@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {

	Optional<NotificationLog> findByEventId(String eventId);

	Optional<NotificationLog> findByCorrelationId(String correlationId);

	List<NotificationLog> findByRecipientEmail(String recipientEmail);

	List<NotificationLog> findByStatus(NotificationStatus status);

}