package com.kartyavya.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Single CORS authority for all browser traffic.
 */
@Configuration
public class GatewayCorsConfig {

    private static final List<String> ALLOWED_METHODS = List.of(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name(),
            HttpMethod.HEAD.name()
    );

    private static final List<String> ALLOWED_HEADERS = List.of(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.ACCEPT,
            HttpHeaders.ORIGIN,
            "X-Requested-With",
            "X-Correlation-Id"
    );

    private static final List<String> EXPOSED_HEADERS = List.of(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_DISPOSITION,
            "X-Correlation-Id"
    );

    private final Set<String> allowedOrigins;

    public GatewayCorsConfig(
            @Value("${KARTYAVYA_CORS_ALLOWED_ORIGINS:http://localhost:5173}")
            String configuredOrigins
    ) {
        this.allowedOrigins = Arrays.stream(configuredOrigins.split(","))
                .map(GatewayCorsConfig::normalizeOrigin)
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (allowedOrigins.isEmpty()) {
            throw new IllegalStateException(
                    "KARTYAVYA_CORS_ALLOWED_ORIGINS must contain at least one browser origin"
            );
        }

        if (allowedOrigins.contains("*")) {
            throw new IllegalStateException(
                    "Wildcard CORS origins are not allowed when credentials are enabled"
            );
        }
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    WebFilter gatewayCorsFilter() {
        return (exchange, chain) -> {
            String suppliedOrigin = exchange.getRequest().getHeaders().getOrigin();

            // Non-browser and same-origin service calls do not need CORS headers.
            if (suppliedOrigin == null || suppliedOrigin.isBlank()) {
                return chain.filter(exchange);
            }

            String normalizedOrigin = normalizeOrigin(suppliedOrigin);
            if (!allowedOrigins.contains(normalizedOrigin)) {
                return rejectOrigin(exchange);
            }

            // Register final header sanitation before any downstream response is committed.
            exchange.getResponse().beforeCommit(() -> {
                applyCorsHeaders(exchange, normalizedOrigin);
                return Mono.empty();
            });

            boolean preflight = exchange.getRequest().getMethod() == HttpMethod.OPTIONS
                    && exchange.getRequest().getHeaders()
                    .containsKey(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);

            if (preflight) {
                applyCorsHeaders(exchange, normalizedOrigin);
                exchange.getResponse().setStatusCode(HttpStatus.NO_CONTENT);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }

    private void applyCorsHeaders(ServerWebExchange exchange, String origin) {
        HttpHeaders headers = exchange.getResponse().getHeaders();

        // Remove any value supplied by a downstream MVC service or stale config.
        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
        headers.remove(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);
        headers.remove(HttpHeaders.ACCESS_CONTROL_MAX_AGE);

        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(",", ALLOWED_METHODS));
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(",", ALLOWED_HEADERS));
        headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, String.join(",", EXPOSED_HEADERS));
        headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
        headers.set(HttpHeaders.VARY,
                "Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
    }

    private Mono<Void> rejectOrigin(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = "{\"message\":\"CORS origin is not allowed\"}"
                .getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body))
        );
    }

    private static String normalizeOrigin(String origin) {
        if (origin == null) {
            return "";
        }
        String normalized = origin.trim();
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
