package com.kartyavya.access.security;

import com.kartyavya.access.entity.User;
import com.kartyavya.access.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter — no Spring context, no DB, no Config Server.
 * Uses MockHttpServletRequest/Response (spring-test) to drive OncePerRequestFilter.doFilter().
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private Claims mockClaims;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userRepository);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthorizationHeader_passesThrough_unauthenticated() throws Exception {
        // No Authorization header at all
        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull(); // chain was called
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    void expiredToken_clearsContextAndPassesThrough() throws Exception {
        request.addHeader("Authorization", "Bearer expired.jwt.token");
        when(jwtService.validateAndExtractClaims("expired.jwt.token"))
            .thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void wrongSignature_clearsContextAndPassesThrough() throws Exception {
        request.addHeader("Authorization", "Bearer tampered.jwt.token");
        when(jwtService.validateAndExtractClaims("tampered.jwt.token"))
            .thenThrow(new io.jsonwebtoken.security.SignatureException("Signature invalid"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void wrongIssuerOrAudience_clearsContextAndPassesThrough() throws Exception {
        request.addHeader("Authorization", "Bearer wrong.iss.aud.token");
        when(jwtService.validateAndExtractClaims("wrong.iss.aud.token"))
            .thenThrow(new io.jsonwebtoken.JwtException("Issuer/audience mismatch"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void validToken_disabledUser_clearsContextAndPassesThrough() throws Exception {
        request.addHeader("Authorization", "Bearer valid.but.disabled.token");

        when(jwtService.validateAndExtractClaims("valid.but.disabled.token")).thenReturn(mockClaims);
        when(mockClaims.getSubject()).thenReturn("1");

        User disabledUser = new User();
        disabledUser.setId(1L);
        disabledUser.setEmail("disabled@test.com");
        disabledUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(disabledUser));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void validToken_enabledUser_setsAuthenticationWithCorrectPrincipal() throws Exception {
        request.addHeader("Authorization", "Bearer valid.citizen.token");

        when(jwtService.validateAndExtractClaims("valid.citizen.token")).thenReturn(mockClaims);
        when(mockClaims.getSubject()).thenReturn("42");
        when(mockClaims.get("role", String.class)).thenReturn("CITIZEN");
        when(mockClaims.get("departmentId", Long.class)).thenReturn(null);

        User enabledUser = new User();
        enabledUser.setId(42L);
        enabledUser.setEmail("citizen@test.com");
        enabledUser.setEnabled(true);
        when(userRepository.findById(42L)).thenReturn(Optional.of(enabledUser));

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(AuthenticatedPrincipal.class);

        AuthenticatedPrincipal principal = (AuthenticatedPrincipal) auth.getPrincipal();
        assertThat(principal.userId()).isEqualTo(42L);
        assertThat(principal.email()).isEqualTo("citizen@test.com");
        assertThat(principal.role()).isEqualTo("CITIZEN");
        assertThat(principal.departmentId()).isNull();
        assertThat(auth.getAuthorities())
            .anyMatch(a -> a.getAuthority().equals("ROLE_CITIZEN"));
    }
}
