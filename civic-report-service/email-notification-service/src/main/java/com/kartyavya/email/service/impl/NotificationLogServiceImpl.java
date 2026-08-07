package com.kartyavya.email.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kartyavya.email.dto.response.NotificationResponse;
import com.kartyavya.email.entity.FailedNotification;
import com.kartyavya.email.entity.NotificationLog;
import com.kartyavya.email.exception.NotificationNotFoundException;
import com.kartyavya.email.mail.EmailService;
import com.kartyavya.email.mapper.NotificationMapper;
import com.kartyavya.email.repository.FailedNotificationRepository;
import com.kartyavya.email.repository.NotificationLogRepository;
import com.kartyavya.email.service.NotificationLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationLogServiceImpl implements NotificationLogService {

	private final NotificationLogRepository notificationLogRepository;

	private final FailedNotificationRepository failedNotificationRepository;

	private final NotificationMapper notificationMapper;

	private final EmailService emailService;

	@Override
	public List<NotificationResponse> getAllNotifications() {

		return notificationLogRepository.findAll().stream().map(notificationMapper::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	public List<NotificationResponse> getNotificationsByRecipientEmail(String recipientEmail) {

		return notificationLogRepository.findByRecipientEmail(recipientEmail).stream()
				.map(notificationMapper::toResponse).collect(Collectors.toList());
	}

	@Override
	public NotificationResponse getNotificationById(String id) {

		NotificationLog notification = notificationLogRepository.findById(id)
				.orElseThrow(() -> new NotificationNotFoundException("Notification not found with id : " + id));

		return notificationMapper.toResponse(notification);
	}

	@Override
	public void retryNotification(String id) {

		FailedNotification failedNotification = failedNotificationRepository.findById(id)
				.orElseThrow(() -> new NotificationNotFoundException("Failed notification not found with id : " + id));

		emailService.sendEmail(failedNotification.getRecipientEmail(), failedNotification.getSubject(),
				failedNotification.getBody());

		failedNotificationRepository.delete(failedNotification);
	}

}