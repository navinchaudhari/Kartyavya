package com.kartyavya.email.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.kartyavya.email.exception.EmailSendingException;
import com.kartyavya.email.util.StringUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

	private final JavaMailSender mailSender;

	@Value("${email.from}")
	private String fromEmail;

	@Override
	public void sendEmail(String recipientEmail, String subject, String body) {

		if (!StringUtil.hasText(recipientEmail)) {
			throw new EmailSendingException("Recipient email cannot be empty.");
		}

		if (!StringUtil.hasText(subject)) {
			throw new EmailSendingException("Email subject cannot be empty.");
		}

		if (!StringUtil.hasText(body)) {
			throw new EmailSendingException("Email body cannot be empty.");
		}

		try {

			MimeMessage mimeMessage = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(recipientEmail);
			helper.setSubject(subject);
			helper.setText(body, true);

			mailSender.send(mimeMessage);

		} catch (Exception ex) {

			throw new EmailSendingException("Failed to send email.", ex);
		}
	}
}