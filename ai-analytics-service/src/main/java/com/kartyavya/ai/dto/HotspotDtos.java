package com.kartyavya.ai.dto;

import java.time.Instant;
import java.util.List;

public final class HotspotDtos {
    private HotspotDtos() {
    }

    public record Hotspot(
            String name,
            double centerLatitude,
            double centerLongitude,
            double radiusKm,
            String priority,
            String dominantCategory,
            String dominantSeverity,
            long complaintCount,
            List<String> areas,
            String explanation,
            String recommendedAction
    ) {
    }

    /**
     * Hotspot analysis remains a successful dashboard response even when the
     * external provider is temporarily unavailable. No keyword or fabricated
     * hotspot fallback is generated; status fields explicitly describe the
     * degraded state.
     */
    public record Analysis(
            String summary,
            List<Hotspot> hotspots,
            String modelVersion,
            Instant generatedAt,
            int sourceComplaintCount,
            boolean configured,
            boolean available,
            String status
    ) {
    }
}
