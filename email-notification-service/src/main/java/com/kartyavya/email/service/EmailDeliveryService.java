package com.kartyavya.email.service;

import com.kartyavya.email.document.NotificationLog;
import com.kartyavya.email.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.Instant;

@Service
public class EmailDeliveryService {
	private static final Logger log = LoggerFactory.getLogger(EmailDeliveryService.class);
	private static final String OTP_EVENT = "password.otp.requested";

	private final JavaMailSender mail;
	private final NotificationLogRepository logs;

	@Value("${email.delivery.enabled:false}")
	private boolean enabled;

	@Value("${email.from:no-reply@kartyavya.local}")
	private String from;

	public EmailDeliveryService(JavaMailSender mail, NotificationLogRepository logs) {
		this.mail = mail;
		this.logs = logs;
	}

	public void send(String eventId, String to, String subject, String html, String type) {
		if (to == null || to.isBlank()) {
			return;
		}

		NotificationLog entry = new NotificationLog();
		entry.setEventId(eventId);
		entry.setRecipient(to);
		entry.setSubject(subject);
		entry.setHtmlBody(OTP_EVENT.equals(type) ? "[REDACTED: one-time password email]" : html);
		entry.setType(type);
		entry.setStatus("PENDING");
		logs.save(entry);

		try {
			if (!enabled) {
				entry.setStatus("PREVIEW");
				log.info("Email preview recorded: type={}, recipient={}, subject={}", type, to, subject);
			} else {
				MimeMessage message = mail.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
				helper.setFrom(from);
				helper.setTo(to);
				helper.setSubject(subject);
				helper.setText(html, true);
				mail.send(message);
				entry.setStatus("SENT");
				entry.setSentAt(Instant.now());
			}
		} catch (Exception exception) {
			entry.setStatus("FAILED");
			entry.setError(safeError(exception));
			log.error("Email delivery failed: type={}, recipient={}", type, to, exception);
		}
		logs.save(entry);
	}

	public String wrap(String title, String messageHtml) {
		return "<div style='font-family:Arial,sans-serif;background:#f1f5f9;padding:30px'>"
				+ "<div style='max-width:620px;margin:auto;background:white;border-radius:16px;overflow:hidden;box-shadow:0 10px 30px #0001'>"
				+ "<div style='background:linear-gradient(135deg,#2563eb,#06b6d4);padding:25px;color:white'>"
				+ "<h2 style='margin:0'>Kartyavya</h2></div>" + "<div style='padding:30px'><h3>" + safe(title) + "</h3>"
				+ "<p style='line-height:1.7;color:#475569'>" + messageHtml + "</p></div></div></div>";
	}

	public String safe(String value) {
		return HtmlUtils.htmlEscape(value == null ? "" : value);
	}

	private String safeError(Exception exception) {
		String message = exception.getMessage();
		if (message == null || message.isBlank()) {
			return exception.getClass().getSimpleName();
		}
		return message.length() > 500 ? message.substring(0, 500) : message;
	}
}
