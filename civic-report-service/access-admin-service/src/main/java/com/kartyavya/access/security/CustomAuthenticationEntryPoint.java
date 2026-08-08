package com.kartyavya.access.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Returns 401 AUTHENTICATION_REQUIRED in the canonical error shape (member-ownership.md)
 * when a request reaches a secured endpoint without valid authentication.
 * correlationId is read from MDC — always present because CorrelationIdFilter runs first.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", 401);
        body.put("error", "AUTHENTICATION_REQUIRED");
        body.put("message", "Authentication is required to access this resource");
        body.put("path", request.getRequestURI());
        body.put("correlationId", MDC.get("correlationId"));
        body.put("fieldErrors", null);

        objectMapper.writeValue(response.getWriter(), body);
    }
}
