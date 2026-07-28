package com.kartyavya.access.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Validates the internal service key during Spring context startup.
 * Fails fast (preventing application traffic) if the key is missing or too short.
 */
@Configuration
public class ServiceKeyStartupValidator {

    @Value("${internal.service-key:}")
    private String internalServiceKey;

    @PostConstruct
    public void validate() {
        if (internalServiceKey == null || internalServiceKey.isBlank()
                || internalServiceKey.length() < 16) {
            throw new IllegalStateException(
                "internal.service-key must be non-blank and at least 16 characters. " +
                "Set via INTERNAL_SERVICE_KEY environment variable.");
        }
    }
}
