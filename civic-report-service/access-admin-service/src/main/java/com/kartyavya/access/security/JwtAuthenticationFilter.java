package com.kartyavya.access.security;

import com.kartyavya.access.entity.User;
import com.kartyavya.access.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the Bearer JWT from the Authorization header, validates it via JwtService, loads the user,
 * checks the enabled flag, and populates SecurityContextHolder.
 *
 * Filter ordering (see SecurityConfig):
 *   CorrelationIdFilter  →  JwtAuthenticationFilter  →  UsernamePasswordAuthenticationFilter
 *
 * Contract:
 *   - Missing token      → pass through unauthenticated (CustomAuthenticationEntryPoint handles downstream).
 *   - Any JWT failure    → clear context, pass through. NEVER throw past this filter.
 *   - disabled user      → clear context, pass through.
 *   - Valid token+user   → set AuthenticatedPrincipal in SecurityContextHolder.
 *
 * Not annotated @Component — declared as a @Bean in SecurityConfig to prevent
 * double-registration in the servlet filter chain.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No bearer token — pass through; entry point handles unauthenticated requests
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        Claims claims;
        try {
            claims = jwtService.validateAndExtractClaims(token);
        } catch (Exception e) {
            // Expired, wrong signature, wrong iss/aud, malformed — clear and continue unauthenticated
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        Long userId;
        try {
            userId = Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isEnabled()) {
            // User deleted or disabled after token was issued — do not authenticate
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        String role = claims.get("role", String.class);
        Long departmentId = claims.get("departmentId", Long.class); // null if claim absent

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            userId, user.getEmail(), role, departmentId
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal, null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
