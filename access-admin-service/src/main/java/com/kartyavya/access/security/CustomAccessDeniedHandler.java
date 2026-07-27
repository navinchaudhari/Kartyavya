package com.kartyavya.access.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Returns 403 ACCESS_DENIED in the canonical error shape (member-ownership.md)
 * when an authenticated principal lacks the required role for an endpoint.
 * correlationId is read from MDC — always present because CorrelationIdFilter runs first.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", 403);
        body.put("error", "ACCESS_DENIED");
        body.put("message", "You do not have permission to access this resource");
        body.put("path", request.getRequestURI());
        body.put("correlationId", MDC.get("correlationId"));
        body.put("fieldErrors", null);

        objectMapper.writeValue(response.getWriter(), body);
    }
}
