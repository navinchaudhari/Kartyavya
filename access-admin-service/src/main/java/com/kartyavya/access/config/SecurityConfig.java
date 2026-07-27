package com.kartyavya.access.config;

import com.kartyavya.access.repository.UserRepository;
import com.kartyavya.access.security.CustomAccessDeniedHandler;
import com.kartyavya.access.security.CustomAuthenticationEntryPoint;
import com.kartyavya.access.security.JwtAuthenticationFilter;
import com.kartyavya.access.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration.
 *
 * Filter chain order (both added before UsernamePasswordAuthFilter; stable sort preserves insertion order):
 *   1. CorrelationIdFilter   — sets MDC + X-Correlation-Id header for ALL requests
 *   2. JwtAuthenticationFilter — validates Bearer token, populates SecurityContext
 *   3. UsernamePasswordAuthenticationFilter (Spring default, unused in our stateless API)
 *
 * Public paths: POST /api/auth/register, POST /api/auth/login
 * All other paths: authenticated
 *
 * Filters are declared as @Bean here (not @Component) to prevent the servlet container
 * auto-registering them as standalone filters (which would cause them to run twice).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomAuthenticationEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtService jwtService,
                          UserRepository userRepository,
                          CustomAuthenticationEntryPoint authEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userRepository);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**"
                ).permitAll()
                .requestMatchers(
                    HttpMethod.GET,
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            // CorrelationIdFilter registered FIRST — Spring Security's stable sort keeps it
            // before JwtAuthenticationFilter when both share the same insertion position.
            .addFilterBefore(correlationIdFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
