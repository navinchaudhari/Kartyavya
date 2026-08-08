package com.kartyavya.access.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalServiceKeyFilterTest {

    private final String VALID_KEY = "super_secret_internal_key_123456";
    
    @Spy private ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    @Mock private FilterChain filterChain;
    
    private InternalServiceKeyFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void initFilter() {
        filter = new InternalServiceKeyFilter(VALID_KEY, objectMapper);
    }

    @Test
    void doFilterInternal_skipNonInternalPath_callsChain() throws Exception {
        initFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/departments");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_missingHeader_returns401AndBlocks() throws Exception {
        initFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/internal/routing/resolve");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getContentAsString()).contains("INTERNAL_SERVICE_AUTH_FAILED");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_wrongKey_returns401AndBlocks() throws Exception {
        initFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/internal/routing/resolve");
        request.addHeader("X-Internal-Service-Key", "wrong_key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
    }

    @Test
    void doFilterInternal_validKey_setsInternalRoleAndCallsChain() throws Exception {
        initFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/internal/routing/resolve");
        request.addHeader("X-Internal-Service-Key", VALID_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).hasSize(1);
        assertThat(auth.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_INTERNAL_SERVICE");
    }

    @Test
    void doFilterInternal_principalReplacement_overwritesExistingContext() throws Exception {
        initFilter();
        
        // Pre-populate context (simulating JwtAuthenticationFilter running first)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(null, null, List.of(new SimpleGrantedAuthority("ROLE_CITIZEN")))
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/internal/routing/resolve");
        request.addHeader("X-Internal-Service-Key", VALID_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        
        // Assert ROLE_CITIZEN is gone, replaced entirely by ROLE_INTERNAL_SERVICE
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_INTERNAL_SERVICE");
    }
}
