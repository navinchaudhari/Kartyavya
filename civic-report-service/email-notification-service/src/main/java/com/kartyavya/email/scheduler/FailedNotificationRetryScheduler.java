package com.kartyavya.email.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kartyavya.email.entity.FailedNotification;
import com.kartyavya.email.entity.NotificationLog;
import com.kartyavya.email.enums.NotificationStatus;
import com.kartyavya.email.enums.NotificationType;
import com.kartyavya.email.mail.EmailService;
import com.kartyavya.email.repository.FailedNotificationRepository;
import com.kartyavya.email.repository.NotificationLogRepository;
import com.kartyavya.email.service.RetryService;
import com.kartyavya.email.util.DateTimeUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FailedNotificationRetryScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FailedNotificationRetryScheduler.class);

	private static final int MAX_RETRY_COUNT = 3;

	private final FailedNotificationRepository failedNotificationRepository;

	private final NotificationLogRepository notificationLogRepository;

	private final EmailService emailService;

	private final RetryService retryService;

	/**
	 * Retry failed notifications every 5 minutes.
	 */
	@Scheduled(fixedDelay = 300000)
	public void retryFailedNotifications() {

		LOGGER.info("Started retry scheduler.");

		List<FailedNotification> failedNotifications = failedNotificationRepository
				.findByRetryCountLessThan(MAX_RETRY_COUNT);

		if (failedNotifications.isEmpty()) {

			LOGGER.info("No failed notifications found.");

			return;
		}

		for (FailedNotification notification : failedNotifications) {

			try {

				LOGGER.info("Retrying notification. EventId={}, RetryCount={}", notification.getEventId(),
						notification.getRetryCount());

				emailService.sendEmail(notification.getRecipientEmail(), notification.getSubject(),
						notification.getBody());

				NotificationLog notificationLog = buildNotificationLog(notification);

				notificationLogRepository.save(notificationLog);

				failedNotificationRepository.delete(notification);

				LOGGER.info("Retry successful. EventId={}", notification.getEventId());

			} catch (Exception ex) {

				LOGGER.error("Retry failed. EventId={}", notification.getEventId(), ex);

				retryService.incrementRetryCount(notification.getEventId());
			}
		}

		LOGGER.info("Retry scheduler completed.");
	}

	private NotificationLog buildNotificationLog(FailedNotification notification) {

		return NotificationLog.builder().eventId(notification.getEventId())
				.recipientEmail(notification.getRecipientEmail()).notificationType(notification.getNotificationType())
				.subject(notification.getSubject()).body(notification.getBody()).status(NotificationStatus.SENT)
				.sentAt(DateTimeUtil.now()).correlationId(notification.getCorrelationId()).build();
	}

}