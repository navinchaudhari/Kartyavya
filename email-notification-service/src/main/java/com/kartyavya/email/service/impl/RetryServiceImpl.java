package com.kartyavya.email.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kartyavya.email.entity.FailedNotification;
import com.kartyavya.email.enums.NotificationType;
import com.kartyavya.email.repository.FailedNotificationRepository;
import com.kartyavya.email.service.RetryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetryServiceImpl implements RetryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RetryServiceImpl.class);

	private final FailedNotificationRepository failedNotificationRepository;

	@Override
	public void saveFailure(String eventId, String errorMessage, String payload, String recipientEmail, String subject,
			String body, NotificationType notificationType) {
		// TODO Auto-generated method stub
		FailedNotification failedNotification = FailedNotification.builder().eventId(eventId).payload(payload)
				.recipientEmail(recipientEmail).subject(subject).body(body).notificationType(notificationType)
				.errorMessage(errorMessage).retryCount(0).build();
		failedNotificationRepository.save(failedNotification);

		LOGGER.error("Notification failure stored for event {}", eventId);
	}

	@Override
	public void incrementRetryCount(String eventId) {

		failedNotificationRepository.findByEventId(eventId).stream().findFirst().ifPresent(notification -> {

			notification.setRetryCount(notification.getRetryCount() + 1);

			failedNotificationRepository.save(notification);

			LOGGER.info("Retry count incremented for event {}", eventId);
		});

	}

}