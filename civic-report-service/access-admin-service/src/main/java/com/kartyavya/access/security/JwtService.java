package com.kartyavya.access.security;

import com.kartyavya.access.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Issues and validates HS256 JWTs.
 *
 * Token claims (frozen per api-contracts.md):
 *   sub = user.id (String), email, role, departmentId (null unless DEPARTMENT_OFFICER),
 *   iss = kartyavya-access-service, aud = kartyavya-api, jti, iat, exp
 *
 * Constructor-injected values allow direct instantiation in unit tests without Spring context.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiryMinutes;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiry-minutes}") long expiryMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMinutes = expiryMinutes;
    }

    public String generateToken(User user, String role, Long departmentId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expiryMinutes, ChronoUnit.MINUTES);

        var builder = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(user.getId().toString())
            .issuer("kartyavya-access-service")
            .audience().add("kartyavya-api").and()
            .claim("email", user.getEmail())
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey);

        // departmentId is only added to the token when present — absent claim reads as null in the filter
        if (departmentId != null) {
            builder.claim("departmentId", departmentId);
        }

        return builder.compact();
    }

    /**
     * Validates signature, expiry, and issuer via the jjwt parser, then manually checks audience.
     * Throws {@link JwtException} on ANY failure — caller must catch and handle.
     */
    public Claims validateAndExtractClaims(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .requireIssuer("kartyavya-access-service")
            .build()
            .parseSignedClaims(token)
            .getPayload();

        // jjwt 0.12.x returns audience as Set<String>; check membership explicitly
        Set<String> audience = claims.getAudience();
        if (audience == null || !audience.contains("kartyavya-api")) {
            throw new JwtException("Token audience validation failed — expected kartyavya-api, got: " + audience);
        }

        return claims;
    }

    public long getExpiryMinutes() {
        return expiryMinutes;
    }
}
