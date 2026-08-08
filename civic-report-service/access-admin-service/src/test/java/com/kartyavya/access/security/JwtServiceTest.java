package com.kartyavya.access.security;

import com.kartyavya.access.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtService — no Spring context, no DB, no Config Server.
 * JwtService is instantiated directly with a test secret to avoid @Value injection.
 */
class JwtServiceTest {

    // 44 chars = 44 bytes > 32-byte minimum for HS256
    private static final String TEST_SECRET = "test-jwt-secret-that-is-at-least-32-bytes!!";
    private static final long EXPIRY_MINUTES = 60L;

    private JwtService jwtService;
    private SecretKey testKey;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Direct construction — @Value annotations are metadata only, ignored here
        jwtService = new JwtService(TEST_SECRET, EXPIRY_MINUTES);
        testKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

        testUser = new User();
        testUser.setId(42L);
        testUser.setEmail("jwt-test@example.com");
        testUser.setName("JWT Test User");
        testUser.setEnabled(true);
    }

    @Test
    void generateToken_decodesCorrectClaims() {
        String token = jwtService.generateToken(testUser, "CITIZEN", null);
        Claims claims = jwtService.validateAndExtractClaims(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("email", String.class)).isEqualTo("jwt-test@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("CITIZEN");
        assertThat(claims.get("departmentId")).isNull();          // absent claim → null
        assertThat(claims.getIssuer()).isEqualTo("kartyavya-access-service");
        assertThat(claims.getAudience()).contains("kartyavya-api");
        assertThat(claims.getId()).isNotBlank();                   // jti present
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void generateToken_withDepartmentId_includesClaimCorrectly() {
        String token = jwtService.generateToken(testUser, "DEPARTMENT_OFFICER", 7L);
        Claims claims = jwtService.validateAndExtractClaims(token);

        assertThat(claims.get("role", String.class)).isEqualTo("DEPARTMENT_OFFICER");
        assertThat(claims.get("departmentId", Long.class)).isEqualTo(7L);
    }

    @Test
    void expiredToken_isRejected() {
        Instant past = Instant.now().minus(2, ChronoUnit.MINUTES);
        String expiredToken = Jwts.builder()
            .subject("42")
            .issuer("kartyavya-access-service")
            .audience().add("kartyavya-api").and()
            .issuedAt(Date.from(past.minus(3, ChronoUnit.MINUTES)))
            .expiration(Date.from(past))
            .signWith(testKey)
            .compact();

        assertThatThrownBy(() -> jwtService.validateAndExtractClaims(expiredToken))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void tamperedToken_wrongSignature_isRejected() {
        String validToken = jwtService.generateToken(testUser, "CITIZEN", null);
        char last = validToken.charAt(validToken.length() - 1);
        // Flip last character — corrupts the signature segment
        String tampered = validToken.substring(0, validToken.length() - 1) +
                          (last == 'A' ? 'B' : 'A');

        assertThatThrownBy(() -> jwtService.validateAndExtractClaims(tampered))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void wrongIssuer_isRejected() {
        Instant now = Instant.now();
        String wrongIssuerToken = Jwts.builder()
            .subject("42")
            .issuer("not-kartyavya-at-all")
            .audience().add("kartyavya-api").and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(60, ChronoUnit.MINUTES)))
            .signWith(testKey)
            .compact();

        assertThatThrownBy(() -> jwtService.validateAndExtractClaims(wrongIssuerToken))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void wrongAudience_isRejected() {
        Instant now = Instant.now();
        String wrongAudToken = Jwts.builder()
            .subject("42")
            .issuer("kartyavya-access-service")
            .audience().add("wrong-service").and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(60, ChronoUnit.MINUTES)))
            .signWith(testKey)
            .compact();

        // Our manual audience check throws JwtException when "kartyavya-api" is not in the set
        assertThatThrownBy(() -> jwtService.validateAndExtractClaims(wrongAudToken))
            .isInstanceOf(JwtException.class);
    }
}
