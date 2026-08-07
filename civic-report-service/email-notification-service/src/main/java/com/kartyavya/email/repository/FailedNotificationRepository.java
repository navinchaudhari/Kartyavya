package com.kartyavya.email.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.kartyavya.email.entity.FailedNotification;
/*
 * Stores failed email deliveries.(Track failures and support retries)
	- Retry Scheduler
	- Manual Retry API
	- Failure Dashboard
 */
@Repository
public interface FailedNotificationRepository extends MongoRepository<FailedNotification, String> {

	List<FailedNotification> findByEventId(String eventId);

	List<FailedNotification> findByRetryCountLessThan(Integer retryCount);

}