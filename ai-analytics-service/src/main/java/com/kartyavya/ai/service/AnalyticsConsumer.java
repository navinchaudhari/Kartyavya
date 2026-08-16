package com.kartyavya.ai.service;

import com.fasterxml.jackson.databind.*;
import com.kartyavya.ai.document.*;
import com.kartyavya.ai.repository.*;
import com.kartyavya.contracts.RabbitTopology;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class AnalyticsConsumer {
	private final ObjectMapper json;
	private final ReportSnapshotRepository reports;
	private final ProcessedEventRepository processed;

	public AnalyticsConsumer(ObjectMapper j, ReportSnapshotRepository r, ProcessedEventRepository p) {
		json = j;
		reports = r;
		processed = p;
	}

	@RabbitListener(queues = RabbitTopology.ANALYTICS_QUEUE)
	public void receive(Message message) throws Exception {
		JsonNode root = json.readTree(message.getBody());
		String eventId = root.path("eventId").asText();
		String type = root.path("eventType").asText();
		if (processed.existsById(eventId))
			return;
		JsonNode d = root.path("data");
		if ("report.created".equals(type)) {
			ReportSnapshot s = new ReportSnapshot();
			s.setReportId(d.path("reportId").asLong());
			s.setTrackingCode(d.path("trackingCode").asText());
			s.setCitizenId(d.path("citizenId").asLong());
			s.setTitle(d.path("title").asText());
			s.setAreaLocation(d.path("areaLocation").asText());
			s.setLatitude(d.path("latitude").asDouble());
			s.setLongitude(d.path("longitude").asDouble());
			s.setCategory(d.path("category").asText());
			s.setSeverity(d.path("severity").asText());
			s.setConfidence(d.path("confidence").asDouble());
			if (!d.path("departmentId").isNull())
				s.setDepartmentId(d.path("departmentId").asLong());
			s.setDepartmentName(d.path("departmentName").asText(null));
			if (!d.path("officerId").isNull())
				s.setOfficerId(d.path("officerId").asLong());
			s.setOfficerName(d.path("officerName").asText(null));
			s.setStatus(d.path("status").asText());
			s.setCreatedAt(Instant.parse(d.path("createdAt").asText()));
			reports.save(s);
		} else if ("report.department.pending".equals(type) || "report.officer.pending".equals(type)) {
			update(d.path("reportId").asLong(), s -> s.setStatus(d.path("status").asText()));
		} else if ("report.officer.assigned".equals(type)) {
			update(d.path("reportId").asLong(), s -> {
				s.setOfficerName(d.path("officerName").asText());
				s.setDepartmentName(d.path("departmentName").asText());
				s.setStatus("ASSIGNED");
			});
		} else if ("report.status.changed".equals(type)) {
			update(d.path("reportId").asLong(), s -> s.setStatus(d.path("currentStatus").asText()));
		} else if ("report.resolved".equals(type)) {
			update(d.path("reportId").asLong(), s -> {
				s.setStatus("RESOLVED");
				s.setResolvedAt(Instant.parse(d.path("resolvedAt").asText()));
			});
		} else if ("classification.corrected".equals(type)) {
			update(d.path("reportId").asLong(), s -> {
				s.setCategory(d.path("correctedCategory").asText());
				s.setSeverity(d.path("correctedSeverity").asText());
				s.setAiOverridden(true);
			});
		}
		processed.save(new ProcessedEvent(eventId, type));
	}

	private void update(long id, java.util.function.Consumer<ReportSnapshot> c) {
		reports.findByReportId(id).ifPresent(s -> {
			c.accept(s);
			s.setUpdatedAt(Instant.now());
			reports.save(s);
		});
	}
}
