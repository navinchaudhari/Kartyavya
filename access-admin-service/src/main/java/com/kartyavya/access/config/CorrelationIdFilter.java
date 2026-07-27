package com.kartyavya.access.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * FIRST filter in the Spring Security chain — runs before JwtAuthenticationFilter on ALL requests
 * (public and secured). Guarantees that every request has a correlationId in MDC and in the
 * X-Correlation-Id response header before any other processing occurs.
 *
 * Not annotated @Component — declared as @Bean in SecurityConfig to prevent double-registration
 * in the servlet container filter chain.
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        } else {
            // Validate that the supplied value is a UUID; generate a fresh one if not
            try {
                UUID.fromString(correlationId);
            } catch (IllegalArgumentException e) {
                correlationId = UUID.randomUUID().toString();
            }
        }

        // Make available to GlobalExceptionHandler and the two security handlers via MDC
        MDC.put(MDC_KEY, correlationId);
        // Echo on response so clients can correlate logs
        response.setHeader(HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clear MDC to prevent leaking into thread-pool worker's next request
            MDC.remove(MDC_KEY);
        }
    }
}
