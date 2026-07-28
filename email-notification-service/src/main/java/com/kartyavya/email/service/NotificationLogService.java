package com.kartyavya.email.service;

import java.util.List;

import com.kartyavya.email.dto.response.NotificationResponse;

public interface NotificationLogService {

	List<NotificationResponse> getAllNotifications();

	List<NotificationResponse> getNotificationsByRecipientEmail(String recipientEmail);

	NotificationResponse getNotificationById(String id);

	void retryNotification(String id);

}