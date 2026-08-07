package com.kartyavya.email.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kartyavya.email.dto.event.ReportAssignedEvent;
import com.kartyavya.email.dto.event.ReportCreatedEvent;
import com.kartyavya.email.dto.event.ReportResolvedEvent;
import com.kartyavya.email.dto.event.ReportStatusChangedEvent;
import com.kartyavya.email.entity.EmailTemplate;
import com.kartyavya.email.entity.FailedNotification;
import com.kartyavya.email.entity.NotificationLog;
import com.kartyavya.email.enums.NotificationStatus;
import com.kartyavya.email.enums.NotificationType;
import com.kartyavya.email.exception.NotificationException;
import com.kartyavya.email.mail.EmailService;
import com.kartyavya.email.repository.NotificationLogRepository;
import com.kartyavya.email.service.IdempotencyService;
import com.kartyavya.email.service.NotificationService;
import com.kartyavya.email.service.RetryService;
import com.kartyavya.email.service.TemplateService;
import com.kartyavya.email.util.DateTimeUtil;
import com.kartyavya.email.util.JsonUtil;
import com.kartyavya.email.util.StringUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

	private final EmailService emailService;

	private final TemplateService templateService;

	private final NotificationLogRepository notificationLogRepository;

	private final IdempotencyService idempotencyService;

	private final RetryService retryService;

	@Override
	public void processReportCreated(ReportCreatedEvent event) {

		Objects.requireNonNull(event, "ReportCreatedEvent cannot be null.");

		if (!StringUtil.hasText(event.getCitizenEmail())) {

			throw new NotificationException("Citizen email is missing.");
		}

		LOGGER.info("Received REPORT_CREATED Event : {}", event.getEventId());

		/*
		 * Duplicate Check
		 */

		if (checkDuplicate(event.getEventId())) {
			return;
		}

		/*
		 * Load Template
		 */

		EmailTemplate template = loadTemplate(NotificationType.REPORT_CREATED.name());
		/*
		 * Prepare Variables
		 */

		Map<String, Object> variables = prepareCreatedVariables(event);

		/*
		 * Generate Subject & Body
		 */

		String subject = renderSubject(template, variables);

		String body = renderBody(template, variables);

		try {

			sendEmail(event.getCitizenEmail(), subject, body);

			NotificationLog log = buildNotificationLog(event.getEventId(), event.getCitizenEmail(),
					NotificationType.REPORT_CREATED, subject, body, event.getCorrelationId());
			saveNotificationLog(log);

			markProcessed(event.getEventId());

			LOGGER.info("REPORT_CREATED notification processed successfully.");

		} catch (Exception ex) {

			handleFailure(event.getEventId(), JsonUtil.toJson(event), event.getCitizenEmail(),
					NotificationType.REPORT_CREATED, subject, body, ex);
			throw ex;
		}

	}

	@Override
	public void processReportAssigned(ReportAssignedEvent event) {

		Objects.requireNonNull(event, "ReportAssignedEvent cannot be null.");

		if (!StringUtil.hasText(event.getOfficerEmail())) {

			throw new NotificationException("Officer email is missing.");
		}
		LOGGER.info("Received REPORT_ASSIGNED Event : {}", event.getEventId());

		if (checkDuplicate(event.getEventId())) {
			return;
		}

		EmailTemplate template = loadTemplate(NotificationType.REPORT_ASSIGNED.name());

		Map<String, Object> variables = prepareAssignedVariables(event);

		String subject = renderSubject(template, variables);

		String body = renderBody(template, variables);

		try {

			sendEmail(event.getOfficerEmail(), subject, body);

			NotificationLog log = buildNotificationLog(event.getEventId(), event.getOfficerEmail(),
					NotificationType.REPORT_ASSIGNED, subject, body, event.getCorrelationId());
			saveNotificationLog(log);

			markProcessed(event.getEventId());

			LOGGER.info("REPORT_ASSIGNED notification processed successfully.");

		} catch (Exception ex) {

			handleFailure(event.getEventId(), JsonUtil.toJson(event), event.getOfficerEmail(),
					NotificationType.REPORT_ASSIGNED, subject, body, ex);
			throw ex;
		}

	}

	@Override
	public void processReportStatusChanged(ReportStatusChangedEvent event) {

		Objects.requireNonNull(event, "ReportStatusChangedEvent cannot be null.");

		if (!StringUtil.hasText(event.getCitizenEmail())) {

			throw new NotificationException("Citizen email is missing.");
		}
		LOGGER.info("Received REPORT_STATUS_CHANGED Event : {}", event.getEventId());

		if (checkDuplicate(event.getEventId())) {
			return;
		}

		EmailTemplate template = loadTemplate(NotificationType.REPORT_STATUS_CHANGED.name());

		Map<String, Object> variables = prepareStatusVariables(event);

		String subject = renderSubject(template, variables);

		String body = renderBody(template, variables);

		try {

			sendEmail(event.getCitizenEmail(), subject, body);

			NotificationLog log = buildNotificationLog(event.getEventId(), event.getCitizenEmail(),
					NotificationType.REPORT_STATUS_CHANGED, subject, body, event.getCorrelationId());

			saveNotificationLog(log);

			markProcessed(event.getEventId());

			LOGGER.info("REPORT_STATUS_CHANGED notification processed successfully.");

		} catch (Exception ex) {

			handleFailure(event.getEventId(), JsonUtil.toJson(event), event.getCitizenEmail(),
					NotificationType.REPORT_STATUS_CHANGED, subject, body, ex);
			throw ex;
		}
	}

	@Override
	public void processReportResolved(ReportResolvedEvent event) {

		Objects.requireNonNull(event, "ReportResolvedEvent cannot be null.");

		if (!StringUtil.hasText(event.getCitizenEmail())) {

			throw new NotificationException("Citizen email is missing.");
		}
		LOGGER.info("Received REPORT_RESOLVED Event : {}", event.getEventId());

		// Step 1 : Duplicate Check
		if (checkDuplicate(event.getEventId())) {
			return;
		}

		// Step 2 : Load Template
		EmailTemplate template = loadTemplate(NotificationType.REPORT_RESOLVED.name());

		// Step 3 : Prepare Variables
		Map<String, Object> variables = prepareResolvedVariables(event);

		// Step 4 : Render Email
		String subject = renderSubject(template, variables);

		String body = renderBody(template, variables);

		try {

			// Step 5 : Send Email
			sendEmail(event.getCitizenEmail(), subject, body);

			// Step 6 : Save Notification Log
			NotificationLog log = buildNotificationLog(event.getEventId(), event.getCitizenEmail(),
					NotificationType.REPORT_RESOLVED, subject, body, event.getCorrelationId());

			saveNotificationLog(log);

			// Step 7 : Mark Event Processed
			markProcessed(event.getEventId());

			LOGGER.info("REPORT_RESOLVED notification processed successfully.");

		} catch (Exception ex) {

			handleFailure(event.getEventId(), JsonUtil.toJson(event), event.getCitizenEmail(),
					NotificationType.REPORT_RESOLVED, subject, body, ex);
			throw ex;
		}
	}

	/*
	 * Helper Methods
	 *
	 */

	private Map<String, Object> prepareCreatedVariables(ReportCreatedEvent event) {

		Map<String, Object> variables = new HashMap<>();

		variables.put("citizenName", event.getCitizenName());

		variables.put("reportId", event.getReportId());

		variables.put("category", event.getCategory());

		variables.put("title", event.getTitle());

		variables.put("eventId", event.getEventId());

		return variables;
	}

	// for processReportAssigned
	private Map<String, Object> prepareAssignedVariables(ReportAssignedEvent event) {

		Map<String, Object> variables = new HashMap<>();

		variables.put("officerName", event.getOfficerName());

		variables.put("officerId", event.getOfficerId());

		variables.put("departmentName", event.getDepartmentName());

		variables.put("reportId", event.getReportId());

		variables.put("eventId", event.getEventId());

		return variables;
	}

	// for processReportStatusChanged
	private Map<String, Object> prepareStatusVariables(ReportStatusChangedEvent event) {

		Map<String, Object> variables = new HashMap<>();

		variables.put("reportId", event.getReportId());

		variables.put("oldStatus", event.getOldStatus());

		variables.put("newStatus", event.getNewStatus());

		variables.put("remarks", event.getRemarks());

		variables.put("eventId", event.getEventId());

		return variables;
	}

	// for processReportResolved
	private Map<String, Object> prepareResolvedVariables(ReportResolvedEvent event) {

		Map<String, Object> variables = new HashMap<>();

		variables.put("reportId", event.getReportId());

		variables.put("resolutionRemarks", event.getResolutionRemarks());

		variables.put("resolvedAt", event.getResolvedAt());

		variables.put("eventId", event.getEventId());

		return variables;
	}

	private boolean checkDuplicate(String eventId) {

		if (idempotencyService.isProcessed(eventId)) {

			LOGGER.warn("Duplicate Event Received : {}", eventId);

			return true;
		}

		return false;
	}

	private EmailTemplate loadTemplate(String templateKey) {

		return templateService.getLatestTemplate(templateKey);
	}

	private String renderSubject(EmailTemplate template, Map<String, Object> variables) {

		return templateService.renderSubject(template, variables);
	}

	private String renderBody(EmailTemplate template, Map<String, Object> variables) {

		return templateService.renderBody(template, variables);
	}

	private NotificationLog buildNotificationLog(String eventId, String recipientEmail,
			NotificationType notificationType, String subject, String body, String correlationId) {
		return NotificationLog.builder().eventId(eventId).recipientEmail(recipientEmail)
				.notificationType(notificationType).subject(subject).body(body).status(NotificationStatus.SENT)
				.sentAt(DateTimeUtil.now()).correlationId(correlationId).build();
	}

	
	private void sendEmail(String recipientEmail, String subject, String body) {

		emailService.sendEmail(recipientEmail, subject, body);

		LOGGER.info("Email sent successfully to {}", recipientEmail);
	}

	private void saveNotificationLog(NotificationLog notificationLog) {

		notificationLogRepository.save(notificationLog);

		LOGGER.info("Notification log saved successfully.");
	}

	private void markProcessed(String eventId) {

		idempotencyService.markProcessed(eventId);

		LOGGER.info("Processed event saved : {}", eventId);
	}

	private void handleFailure(String eventId, String payload, String recipientEmail, NotificationType notificationType,
			String subject, String body, Exception ex) {

		retryService.saveFailure(eventId, ex.getMessage(), payload, recipientEmail, subject, body, notificationType);
		LOGGER.error("Failed to process notification. EventId={}", eventId, ex);
	}

}