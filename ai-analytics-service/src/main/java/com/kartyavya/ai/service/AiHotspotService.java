package com.kartyavya.ai.service;

import com.kartyavya.ai.dto.HotspotDtos;
import com.kartyavya.ai.exception.GeminiApiException;
import com.kartyavya.ai.repository.ReportSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class AiHotspotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiHotspotService.class);

    private final ReportSnapshotRepository repository;
    private final GeminiClient gemini;
    private final Duration cacheDuration;
    private volatile HotspotDtos.Analysis cached;

    public AiHotspotService(
            ReportSnapshotRepository repository,
            GeminiClient gemini,
            @Value("${ai.gemini.hotspot-cache-minutes:5}") long cacheMinutes
    ) {
        this.repository = repository;
        this.gemini = gemini;
        this.cacheDuration = Duration.ofMinutes(Math.max(1, cacheMinutes));
    }

    public synchronized HotspotDtos.Analysis analyze(boolean forceRefresh) {
        if (!forceRefresh && cached != null
                && cached.generatedAt().plus(cacheDuration).isAfter(Instant.now())) {
            return cached;
        }

        var snapshots = repository.findAll();
        try {
            cached = gemini.analyzeHotspots(snapshots);
        } catch (GeminiApiException exception) {
            LOGGER.warn("Gemini hotspot analysis is unavailable: {}", exception.getMessage());
            boolean configured = gemini.configured();
            String status = configured ? "PROVIDER_UNAVAILABLE" : "NOT_CONFIGURED";
            String summary = configured
                    ? "Gemini hotspot analysis is temporarily unavailable. Core administration, analytics, and complaint map data remain available."
                    : "Gemini hotspot analysis is not configured. Add KARTYAVYA_GEMINI_API_KEY to the project-root .env file and restart AI Analytics Service.";

            // This is an explicit degraded response, not a keyword-based AI fallback.
            cached = new HotspotDtos.Analysis(
                    summary,
                    List.of(),
                    gemini.modelVersion(),
                    Instant.now(),
                    snapshots.size(),
                    configured,
                    false,
                    status
            );
        }
        return cached;
    }
}
