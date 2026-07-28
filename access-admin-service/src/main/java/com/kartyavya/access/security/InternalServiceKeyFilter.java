package com.kartyavya.access.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kartyavya.access.dto.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;

/**
 * Secures /internal/** endpoints using the X-Internal-Service-Key header.
 * Runs AFTER JwtAuthenticationFilter in the Spring Security filter chain.
 *
 * <p>Key Contract:
 * <ul>
 *   <li>Skips immediately for paths not starting with /internal/</li>
 *   <li>Rejects request with 401 INTERNAL_SERVICE_AUTH_FAILED if header is missing or incorrect</li>
 *   <li>Uses constant-time string comparison via MessageDigest.isEqual</li>
 *   <li>On success, UNCONDITIONALLY OVERWRITES the SecurityContext with ROLE_INTERNAL_SERVICE.
 *       This ensures /internal/** is inaccessible via JWT alone.</li>
 * </ul>
 *
 * <p>Not a @Component; registered manually in SecurityConfig to control ordering.
 */
public class InternalServiceKeyFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Internal-Service-Key";
    private final String internalServiceKey;
    private final ObjectMapper objectMapper;

    public InternalServiceKeyFilter(String internalServiceKey, ObjectMapper objectMapper) {
        this.internalServiceKey = internalServiceKey;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader(HEADER_NAME);

        if (providedKey == null || !MessageDigest.isEqual(
                internalServiceKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8))) {

            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            ApiErrorResponse apiError = new ApiErrorResponse(
                    Instant.now(),
                    401,
                    "INTERNAL_SERVICE_AUTH_FAILED",
                    "Missing or invalid internal service key",
                    request.getRequestURI(),
                    MDC.get("correlationId"),
                    null
            );

            response.getWriter().write(objectMapper.writeValueAsString(apiError));
            return; // Do NOT call filterChain.doFilter()
        }

        // Unconditional overwrite: even if a JWT was present and valid,
        // we replace the authentication with the INTERNAL_SERVICE role.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(null, null, List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE")))
        );

        filterChain.doFilter(request, response);
    }
}
