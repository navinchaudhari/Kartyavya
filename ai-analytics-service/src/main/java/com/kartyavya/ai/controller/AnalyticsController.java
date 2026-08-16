package com.kartyavya.ai.controller;

import com.kartyavya.ai.document.ReportSnapshot;
import com.kartyavya.ai.dto.HotspotDtos;
import com.kartyavya.ai.repository.ReportSnapshotRepository;
import com.kartyavya.ai.service.AiHotspotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
	private final ReportSnapshotRepository repository;
	private final AiHotspotService hotspotService;

	public AnalyticsController(ReportSnapshotRepository repository, AiHotspotService hotspotService) {
		this.repository = repository;
		this.hotspotService = hotspotService;
	}

	@GetMapping("/overview")
	Object overview() {
		List<ReportSnapshot> reports = repository.findAll();
		List<ReportSnapshot> resolved = reports.stream()
				.filter(report -> report.getResolvedAt() != null && report.getCreatedAt() != null).toList();
		double averageHours = resolved.isEmpty() ? 0
				: resolved.stream()
						.mapToLong(report -> Duration.between(report.getCreatedAt(), report.getResolvedAt()).toHours())
						.average().orElse(0);
		return Map.of("total", reports.size(), "status", group(reports, ReportSnapshot::getStatus), "category",
				group(reports, ReportSnapshot::getCategory), "severity", group(reports, ReportSnapshot::getSeverity),
				"department",
				group(reports, report -> Optional.ofNullable(report.getDepartmentName()).orElse("Unmapped")),
				"averageResolutionHours", Math.round(averageHours * 10) / 10.0, "aiCorrectionRate",
				reports.isEmpty() ? 0
						: Math.round(reports.stream().filter(ReportSnapshot::isAiOverridden).count() * 1000.0
								/ reports.size()) / 10.0,
				"highSeverityOpen",
				reports.stream()
						.filter(report -> "HIGH".equals(report.getSeverity()) && !"RESOLVED".equals(report.getStatus()))
						.count());
	}

	@GetMapping("/heatmap")
	Object heatmap() {
		return repository.findAll().stream()
				.filter(report -> report.getLatitude() != null && report.getLongitude() != null)
				.map(report -> Map.of("complaintId", report.getReportId(), "latitude", report.getLatitude(),
						"longitude", report.getLongitude(), "aiSeverity", report.getSeverity(), "aiCategory",
						report.getCategory(), "aiConfidence",
						report.getConfidence() == null ? 0 : report.getConfidence(), "status", report.getStatus(),
						"areaLocation", report.getAreaLocation(), "complaintTitle", report.getTitle()))
				.toList();
	}

	@GetMapping("/ai-hotspots")
	HotspotDtos.Analysis aiHotspots() {
		return hotspotService.analyze(false);
	}

	@PostMapping("/ai-hotspots/refresh")
	HotspotDtos.Analysis refreshAiHotspots() {
		return hotspotService.analyze(true);
	}

	@GetMapping("/problem-areas")
	Object problemAreas() {
		return group(repository.findAll(), ReportSnapshot::getAreaLocation);
	}

	private List<Map<String, Object>> group(List<ReportSnapshot> reports, Function<ReportSnapshot, String> classifier) {
		return reports.stream()
				.collect(
						Collectors.groupingBy(report -> Optional.ofNullable(classifier.apply(report)).orElse("Unknown"),
								Collectors.counting()))
				.entrySet().stream().map(entry -> {
					Map<String, Object> result = new LinkedHashMap<>();
					result.put("name", entry.getKey());
					result.put("value", entry.getValue());
					return result;
				}).toList();
	}
}
