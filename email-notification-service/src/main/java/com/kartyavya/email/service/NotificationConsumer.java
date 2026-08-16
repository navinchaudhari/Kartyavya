package com.kartyavya.email.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kartyavya.contracts.RabbitTopology;
import com.kartyavya.email.document.ProcessedEvent;
import com.kartyavya.email.repository.ProcessedEventRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
	private final ObjectMapper json;
	private final EmailDeliveryService email;
	private final ProcessedEventRepository processed;

	public NotificationConsumer(ObjectMapper json, EmailDeliveryService email, ProcessedEventRepository processed) {
		this.json = json;
		this.email = email;
		this.processed = processed;
	}

	@RabbitListener(queues = RabbitTopology.EMAIL_QUEUE)
	public void consume(Message message) throws Exception {
		JsonNode root = json.readTree(message.getBody());
		String eventId = root.path("eventId").asText();
		String type = root.path("eventType").asText();
		if (eventId.isBlank() || processed.existsById(eventId)) {
			return;
		}

		JsonNode data = root.path("data");
		switch (type) {
		case "user.registered" ->
			email.send(eventId, text(data, "email"), "Welcome to Kartyavya", email.wrap("Registration successful",
					"Hello " + safe(data, "fullName") + ", your citizen account is ready."), type);
		case "password.otp.requested" ->
			email.send(eventId, text(data, "email"), "Your Kartyavya password reset OTP", email.wrap(
					"Password reset OTP",
					"Your OTP is <b style='font-size:22px'>" + safe(data, "otp") + "</b>. It expires in 10 minutes."),
					type);
		case "password.reset" -> email.send(eventId, text(data, "email"), "Kartyavya password changed",
				email.wrap("Password changed", "Your password was reset successfully."), type);
		case "report.created" -> email.send(eventId, text(data, "citizenEmail"),
				"Complaint registered: " + text(data, "trackingCode"),
				email.wrap("Complaint registered",
						"Your complaint <b>" + safe(data, "title") + "</b> has been registered. " + "Current status: "
								+ safe(data, "status") + ". Tracking code: <b>" + safe(data, "trackingCode") + "</b>."),
				type);
		case "report.department.pending",
				"report.officer.pending" ->
			email.send(eventId, text(data, "citizenEmail"), "Complaint awaiting administrative assignment",
					email.wrap("Assignment pending", safe(data, "pendingReason")
							+ ". Your complaint remains active and an administrator can assign it after setup."),
					type);
		case "report.officer.assigned" -> {
			String subject = "Complaint assigned: " + text(data, "trackingCode");
			email.send(eventId + "-citizen", text(data, "citizenEmail"), subject,
					email.wrap("Officer assigned", "Your complaint has been assigned to " + safe(data, "officerName")
							+ " from " + safe(data, "departmentName") + "."),
					type);
			email.send(eventId + "-officer", text(data, "officerEmail"), "New complaint assigned",
					email.wrap("New assignment", "Complaint: " + safe(data, "title") + "<br>Location: "
							+ safe(data, "areaLocation") + "<br>Severity: " + safe(data, "severity")),
					type);
		}
		case "report.status.changed" -> email.send(eventId, text(data, "citizenEmail"), "Complaint status updated",
				email.wrap("Status updated", "Your complaint moved from " + safe(data, "previousStatus") + " to <b>"
						+ safe(data, "currentStatus") + "</b>."),
				type);
		case "report.resolved" -> email.send(eventId, text(data, "citizenEmail"), "Complaint resolved",
				email.wrap("Resolution completed", safe(data, "resolutionRemark")), type);
		default -> {
			return;
		}
		}

		processed.save(new ProcessedEvent(eventId, type));
	}

	private String safe(JsonNode data, String field) {
		return email.safe(text(data, field));
	}

	private String text(JsonNode data, String field) {
		return data.path(field).asText("");
	}
}
