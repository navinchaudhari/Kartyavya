package com.kartyavya.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC_POST_ENDPOINTS = Set.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/verify-otp",
            "/api/auth/reset-password"
    );

    private static final Set<String> PUBLIC_GET_ENDPOINTS = Set.of(
            "/api/departments",
            "/api/ai/status",
            "/actuator/health"
    );

    private static final String CORRELATION_ID = "X-Correlation-Id";
    private static final String USER_ID = "X-User-Id";
    private static final String USER_ROLE = "X-User-Role";
    private static final String USER_EMAIL = "X-User-Email";

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.audience}")
    private String audience;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = Optional.ofNullable(
                        exchange.getRequest().getHeaders().getFirst(CORRELATION_ID))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        // Identity headers supplied by a browser are untrusted and must be removed.
        ServerWebExchange sanitizedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.remove(USER_ID);
                    headers.remove(USER_ROLE);
                    headers.remove(USER_EMAIL);
                    headers.set(CORRELATION_ID, correlationId);
                }))
                .build();
        sanitizedExchange.getResponse().getHeaders().set(CORRELATION_ID, correlationId);

        if (isPublicRequest(sanitizedExchange)) {
            return chain.filter(sanitizedExchange);
        }

        String authorization = sanitizedExchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(sanitizedExchange, "Missing bearer token");
        }

        String token = authorization.substring(7).trim();
        if (token.isBlank()) {
            return unauthorized(sanitizedExchange, "Missing bearer token");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            String role = claims.get("role", String.class);
            String email = claims.get("email", String.class);

            if (subject == null || subject.isBlank()
                    || role == null || role.isBlank()
                    || email == null || email.isBlank()) {
                return unauthorized(sanitizedExchange, "Required JWT claims are missing");
            }

            ServerWebExchange authenticatedExchange = sanitizedExchange.mutate()
                    .request(request -> request.headers(headers -> {
                        headers.set(USER_ID, subject);
                        headers.set(USER_ROLE, role);
                        headers.set(USER_EMAIL, email);
                        headers.set(CORRELATION_ID, correlationId);
                    }))
                    .build();

            return chain.filter(authenticatedExchange);
        } catch (JwtException | IllegalArgumentException exception) {
            return unauthorized(sanitizedExchange, "Invalid or expired token");
        }
    }

    private boolean isPublicRequest(ServerWebExchange exchange) {
        HttpMethod method = exchange.getRequest().getMethod();
        String path = normalizePath(exchange.getRequest().getURI().getPath());

        if (method == HttpMethod.OPTIONS) {
            return true;
        }
        if (method == HttpMethod.POST && PUBLIC_POST_ENDPOINTS.contains(path)) {
            return true;
        }
        if ((method == HttpMethod.GET || method == HttpMethod.HEAD)
                && PUBLIC_GET_ENDPOINTS.contains(path)) {
            return true;
        }
        if ((method == HttpMethod.GET || method == HttpMethod.HEAD)
                && path.startsWith("/api/reports/track/")) {
            return true;
        }
        return (method == HttpMethod.GET || method == HttpMethod.HEAD)
                && path.startsWith("/uploads/");
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.length() > 1 && path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"message\":\"" + escapeJson(message) + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body))
        );
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
