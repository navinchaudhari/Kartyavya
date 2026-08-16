package com.kartyavya.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kartyavya.ai.document.ReportSnapshot;
import com.kartyavya.ai.dto.HotspotDtos;
import com.kartyavya.ai.exception.GeminiApiException;
import com.kartyavya.contracts.ClassificationContracts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class GeminiClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(GeminiClient.class);
	private static final Set<String> CATEGORIES = Set.of("POTHOLE", "GARBAGE", "STREETLIGHT", "WATER_LEAKAGE", "OTHER");
	private static final Set<String> SEVERITIES = Set.of("LOW", "MEDIUM", "HIGH");
	private static final Set<String> DEPARTMENT_CODES = Set.of("ROADS_INFRASTRUCTURE", "SOLID_WASTE", "ELECTRICAL",
			"WATER_DRAINAGE", "GENERAL_CIVIC");

	private final RestClient client;
	private final ObjectMapper mapper;
	private final boolean enabled;
	private final String apiKey;
	private final String model;
	private final double temperature;
	private final int maxAttempts;
	private final long retryDelayMillis;

	public GeminiClient(RestClient.Builder builder, ObjectMapper mapper,
			@Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com}") String baseUrl,
			@Value("${ai.gemini.enabled:true}") boolean enabled, @Value("${ai.gemini.api-key:}") String apiKey,
			@Value("${ai.gemini.model:gemini-3.1-flash-lite}") String model,
			@Value("${ai.gemini.temperature:0.10}") double temperature,
			@Value("${ai.gemini.connect-timeout-seconds:10}") int connectTimeout,
			@Value("${ai.gemini.read-timeout-seconds:45}") int readTimeout,
			@Value("${ai.gemini.max-attempts:3}") int maxAttempts,
			@Value("${ai.gemini.retry-delay-millis:750}") long retryDelayMillis) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(connectTimeout));
		requestFactory.setReadTimeout(Duration.ofSeconds(readTimeout));
		this.client = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
		this.mapper = mapper;
		this.enabled = enabled;
		this.apiKey = apiKey == null ? "" : apiKey.trim();
		this.model = model == null || model.isBlank() ? "gemini-3.1-flash-lite" : model.trim();
		this.temperature = temperature;
		this.maxAttempts = Math.max(1, Math.min(5, maxAttempts));
		this.retryDelayMillis = Math.max(100, Math.min(5000, retryDelayMillis));
	}

	public boolean configured() {
		return enabled && !apiKey.isBlank() && !apiKey.startsWith("REPLACE_WITH_") && !apiKey.startsWith("CHANGE_ME_")
				&& !apiKey.startsWith("${");
	}

	public String modelVersion() {
		return "gemini:" + model;
	}

	public ClassificationContracts.Response classify(ClassificationContracts.Request request) {
		ensureConfigured();
		String prompt = """
				Classify this municipal civic complaint. The complaint may be written in English,
				Hindi, Marathi, or mixed language. Use semantic meaning, not simple keyword matching.

				Allowed categories:
				POTHOLE, GARBAGE, STREETLIGHT, WATER_LEAKAGE, OTHER

				Allowed severity values:
				LOW, MEDIUM, HIGH

				Allowed suggestedDepartmentCode values:
				ROADS_INFRASTRUCTURE, SOLID_WASTE, ELECTRICAL, WATER_DRAINAGE, GENERAL_CIVIC

				Severity must consider danger to people, public health, traffic impact, affected area,
				urgency, and likely infrastructure damage. Confidence must be from 0 to 100.
				Signals must contain short evidence phrases derived from the supplied complaint only.

				Complaint title: %s
				Complaint description: %s
				""".formatted(request.title(), request.description());

		ObjectNode schema = mapper.createObjectNode();
		schema.put("type", "object");
		ObjectNode properties = schema.putObject("properties");
		enumString(properties, "category", CATEGORIES);
		enumString(properties, "severity", SEVERITIES);
		ObjectNode confidence = properties.putObject("confidence");
		confidence.put("type", "number");
		confidence.put("minimum", 0);
		confidence.put("maximum", 100);
		enumString(properties, "suggestedDepartmentCode", DEPARTMENT_CODES);
		ObjectNode signals = properties.putObject("signals");
		signals.put("type", "array");
		signals.putObject("items").put("type", "string");
		schema.putArray("required").add("category").add("severity").add("confidence").add("suggestedDepartmentCode")
				.add("signals");

		JsonNode result = generateJson(
				"You are Kartyavya's civic complaint classification engine. Return only schema-valid JSON.", prompt,
				schema);

		String category = requiredEnum(result, "category", CATEGORIES);
		String severity = requiredEnum(result, "severity", SEVERITIES);
		String departmentCode = requiredEnum(result, "suggestedDepartmentCode", DEPARTMENT_CODES);
		double confidenceValue = Math.max(0, Math.min(100, result.path("confidence").asDouble()));
		List<String> signalsList = new ArrayList<>();
		result.path("signals").forEach(node -> {
			String value = node.asText().trim();
			if (!value.isBlank() && signalsList.size() < 8) {
				signalsList.add(value);
			}
		});

		return new ClassificationContracts.Response(com.kartyavya.contracts.ReportCategory.valueOf(category),
				com.kartyavya.contracts.Severity.valueOf(severity), confidenceValue, departmentCode,
				List.copyOf(signalsList), modelVersion());
	}

	public HotspotDtos.Analysis analyzeHotspots(List<ReportSnapshot> snapshots) {
		ensureConfigured();
		if (snapshots.isEmpty()) {
			return new HotspotDtos.Analysis("No geocoded complaints are available for hotspot analysis.", List.of(),
					modelVersion(), java.time.Instant.now(), 0, true, true, "NO_DATA");
		}

		List<ReportSnapshot> source = snapshots.stream()
				.filter(item -> item.getLatitude() != null && item.getLongitude() != null).sorted(Comparator
						.comparing(ReportSnapshot::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.limit(250).toList();
		if (source.isEmpty()) {
			return new HotspotDtos.Analysis("No geocoded complaints are available for hotspot analysis.", List.of(),
					modelVersion(), java.time.Instant.now(), 0, true, true, "NO_DATA");
		}

		ArrayNode records = mapper.createArrayNode();
		source.forEach(item -> {
			ObjectNode record = records.addObject();
			record.put("complaintId", item.getReportId());
			record.put("latitude", item.getLatitude());
			record.put("longitude", item.getLongitude());
			record.put("area", item.getAreaLocation());
			record.put("category", item.getCategory());
			record.put("severity", item.getSeverity());
			record.put("status", item.getStatus());
			record.put("confidence", item.getConfidence() == null ? 0 : item.getConfidence());
		});

		String prompt;
		try {
			prompt = """
					Analyze the supplied, real municipal complaint points and identify up to 8 geographic
					civic hotspots. Do not invent complaint records, coordinates, counts, area names, or
					categories. A hotspot should contain spatially close complaints or a meaningful cluster
					in the same named area. Use only the supplied coordinates when calculating a center.
					Prioritize unresolved, HIGH-severity, and repeated-category clusters.

					Return a concise municipal operations summary and ranked hotspots. complaintCount must
					reflect only records assigned to that hotspot. Recommended actions must be practical for
					a local authority.

					Complaint records JSON:
					%s
					""".formatted(mapper.writeValueAsString(records));
		} catch (JsonProcessingException exception) {
			throw new GeminiApiException("Unable to prepare hotspot data for Gemini", exception);
		}

		ObjectNode schema = hotspotSchema();
		JsonNode result = generateJson(
				"You are a municipal geospatial risk analyst. Use only the factual coordinates and complaint data supplied by Kartyavya.",
				prompt, schema);

		List<HotspotDtos.Hotspot> hotspots = new ArrayList<>();
		double minLat = source.stream().mapToDouble(ReportSnapshot::getLatitude).min().orElse(-90);
		double maxLat = source.stream().mapToDouble(ReportSnapshot::getLatitude).max().orElse(90);
		double minLng = source.stream().mapToDouble(ReportSnapshot::getLongitude).min().orElse(-180);
		double maxLng = source.stream().mapToDouble(ReportSnapshot::getLongitude).max().orElse(180);

		result.path("hotspots").forEach(node -> {
			if (hotspots.size() >= 8) {
				return;
			}
			double latitude = node.path("centerLatitude").asDouble(Double.NaN);
			double longitude = node.path("centerLongitude").asDouble(Double.NaN);
			if (!Double.isFinite(latitude) || !Double.isFinite(longitude) || latitude < minLat - 0.02
					|| latitude > maxLat + 0.02 || longitude < minLng - 0.02 || longitude > maxLng + 0.02) {
				return;
			}
			List<String> areas = new ArrayList<>();
			node.path("areas").forEach(area -> {
				if (areas.size() < 6 && !area.asText().isBlank()) {
					areas.add(area.asText().trim());
				}
			});
			hotspots.add(new HotspotDtos.Hotspot(node.path("name").asText("Civic hotspot"), latitude, longitude,
					Math.max(0.1, Math.min(10, node.path("radiusKm").asDouble(1))),
					normalizePriority(node.path("priority").asText()), node.path("dominantCategory").asText("OTHER"),
					node.path("dominantSeverity").asText("LOW"), Math.max(1, node.path("complaintCount").asLong(1)),
					List.copyOf(areas), node.path("explanation").asText(""),
					node.path("recommendedAction").asText("")));
		});

		return new HotspotDtos.Analysis(
				result.path("summary").asText("Gemini analyzed the current geocoded complaint distribution."),
				List.copyOf(hotspots), modelVersion(), java.time.Instant.now(), source.size(), true, true, "AVAILABLE");
	}

	private JsonNode generateJson(String systemInstruction, String prompt, ObjectNode schema) {
		ObjectNode request = mapper.createObjectNode();
		request.putObject("systemInstruction").putArray("parts").addObject().put("text", systemInstruction);
		request.putArray("contents").addObject().put("role", "user").putArray("parts").addObject().put("text", prompt);
		ObjectNode generationConfig = request.putObject("generationConfig");
		generationConfig.put("temperature", temperature);
		generationConfig.put("responseMimeType", "application/json");
		generationConfig.set("responseJsonSchema", schema);

		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				JsonNode response = client.post().uri("/v1beta/models/{model}:generateContent", model)
						.header("x-goog-api-key", apiKey).contentType(MediaType.APPLICATION_JSON).body(request)
						.retrieve().body(JsonNode.class);

				String text = response == null ? ""
						: response.path("candidates").path(0).path("content").path("parts").path(0).path("text")
								.asText();
				if (text.isBlank()) {
					if (attempt < maxAttempts) {
						LOGGER.warn("Gemini returned an empty response on attempt {}/{}", attempt, maxAttempts);
						waitBeforeRetry(attempt);
						continue;
					}
					throw new GeminiApiException("Gemini returned no structured response");
				}
				return mapper.readTree(stripCodeFence(text));
			} catch (RestClientResponseException exception) {
				int status = exception.getStatusCode().value();
				LOGGER.warn("Gemini HTTP failure on attempt {}/{}: status={}, response={}", attempt, maxAttempts,
						status, abbreviate(exception.getResponseBodyAsString()));
				if (isTransient(exception.getStatusCode()) && attempt < maxAttempts) {
					waitBeforeRetry(attempt);
					continue;
				}
				throw new GeminiApiException(providerMessage(status), exception);
			} catch (ResourceAccessException exception) {
				LOGGER.warn("Gemini network failure on attempt {}/{}: {}", attempt, maxAttempts,
						exception.getMessage());
				if (attempt < maxAttempts) {
					waitBeforeRetry(attempt);
					continue;
				}
				throw new GeminiApiException("Gemini could not be reached. Check network access and timeout settings.",
						exception);
			} catch (JsonProcessingException exception) {
				LOGGER.warn("Gemini returned invalid structured JSON on attempt {}/{}", attempt, maxAttempts);
				if (attempt < maxAttempts) {
					waitBeforeRetry(attempt);
					continue;
				}
				throw new GeminiApiException(
						"Gemini returned a response that did not match the required JSON structure.", exception);
			} catch (RestClientException exception) {
				throw new GeminiApiException("Gemini API request failed because of a client or network error.",
						exception);
			}
		}

		throw new GeminiApiException("Gemini API request failed after all retry attempts");
	}

	private boolean isTransient(HttpStatusCode status) {
		return status.value() == 429 || status.is5xxServerError();
	}

	private void waitBeforeRetry(int completedAttempt) {
		long delay = Math.min(5000, retryDelayMillis * (1L << Math.min(3, completedAttempt - 1)));
		try {
			Thread.sleep(delay);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new GeminiApiException("Gemini retry was interrupted", exception);
		}
	}

	private String providerMessage(int status) {
		return switch (status) {
		case 400 -> "Gemini rejected the request. Verify the configured model and structured-output settings.";
		case 401, 403 -> "Gemini rejected the API key or the key does not have permission to use the configured model.";
		case 404 -> "The configured Gemini model was not found. Check KARTYAVYA_GEMINI_MODEL in .env.";
		case 429 -> "Gemini quota or rate limit is exhausted. Retry later or check the active project quota.";
		default -> status >= 500 ? "Gemini is temporarily unavailable after automatic retries."
				: "Gemini API request failed with HTTP status " + status + ".";
		};
	}

	private String abbreviate(String value) {
		if (value == null || value.isBlank()) {
			return "<empty>";
		}
		String singleLine = value.replaceAll("\\s+", " ").trim();
		return singleLine.length() <= 600 ? singleLine : singleLine.substring(0, 600) + "...";
	}

	private void ensureConfigured() {
		if (!configured()) {
			throw new GeminiApiException(
					"Gemini is not configured. Set KARTYAVYA_GEMINI_API_KEY in the project-root .env file, keep KARTYAVYA_GEMINI_ENABLED=true, and restart AI Analytics Service.");
		}
	}

	private static void enumString(ObjectNode properties, String field, Set<String> values) {
		ObjectNode node = properties.putObject(field);
		node.put("type", "string");
		ArrayNode array = node.putArray("enum");
		values.stream().sorted().forEach(array::add);
	}

	private static String requiredEnum(JsonNode result, String field, Set<String> allowed) {
		String value = result.path(field).asText().trim().toUpperCase(Locale.ROOT);
		if (!allowed.contains(value)) {
			throw new GeminiApiException("Gemini returned unsupported " + field + ": " + value);
		}
		return value;
	}

	private ObjectNode hotspotSchema() {
		ObjectNode schema = mapper.createObjectNode();
		schema.put("type", "object");
		ObjectNode properties = schema.putObject("properties");
		properties.putObject("summary").put("type", "string");
		ObjectNode hotspots = properties.putObject("hotspots");
		hotspots.put("type", "array");
		ObjectNode item = hotspots.putObject("items");
		item.put("type", "object");
		ObjectNode fields = item.putObject("properties");
		fields.putObject("name").put("type", "string");
		fields.putObject("centerLatitude").put("type", "number");
		fields.putObject("centerLongitude").put("type", "number");
		fields.putObject("radiusKm").put("type", "number");
		enumString(fields, "priority", Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL"));
		enumString(fields, "dominantCategory", CATEGORIES);
		enumString(fields, "dominantSeverity", SEVERITIES);
		fields.putObject("complaintCount").put("type", "integer");
		ObjectNode areas = fields.putObject("areas");
		areas.put("type", "array");
		areas.putObject("items").put("type", "string");
		fields.putObject("explanation").put("type", "string");
		fields.putObject("recommendedAction").put("type", "string");
		item.putArray("required").add("name").add("centerLatitude").add("centerLongitude").add("radiusKm")
				.add("priority").add("dominantCategory").add("dominantSeverity").add("complaintCount").add("areas")
				.add("explanation").add("recommendedAction");
		schema.putArray("required").add("summary").add("hotspots");
		return schema;
	}

	private static String normalizePriority(String value) {
		String normalized = value == null ? "LOW" : value.trim().toUpperCase(Locale.ROOT);
		return Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(normalized) ? normalized : "LOW";
	}

	private static String stripCodeFence(String value) {
		String cleaned = value.trim();
		if (cleaned.startsWith("```")) {
			int firstNewline = cleaned.indexOf('\n');
			int lastFence = cleaned.lastIndexOf("```");
			if (firstNewline >= 0 && lastFence > firstNewline) {
				cleaned = cleaned.substring(firstNewline + 1, lastFence).trim();
			}
		}
		return cleaned;
	}
}
