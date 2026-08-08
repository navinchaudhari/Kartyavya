package com.kartyavya.access.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kartyavya.access.repository.UserRepository;
import com.kartyavya.access.security.CustomAccessDeniedHandler;
import com.kartyavya.access.security.CustomAuthenticationEntryPoint;
import com.kartyavya.access.security.InternalServiceKeyFilter;
import com.kartyavya.access.security.JwtAuthenticationFilter;
import com.kartyavya.access.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${internal.service-key:}")
    private String internalServiceKey;

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomAuthenticationEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtService jwtService,
                          UserRepository userRepository,
                          CustomAuthenticationEntryPoint authEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.objectMapper = objectMapper;
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
    public InternalServiceKeyFilter internalServiceKeyFilter() {
        return new InternalServiceKeyFilter(internalServiceKey, objectMapper);
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
                .requestMatchers(HttpMethod.GET, "/api/departments").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/departments").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/departments/**").hasRole("ADMIN")
                .requestMatchers("/api/routing-rules/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/internal/**").hasRole("INTERNAL_SERVICE")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            // Filter chain order:
            // 1. CorrelationIdFilter - sets MDC + X-Correlation-Id for ALL requests (runs first)
            // 2. JwtAuthenticationFilter - validates Bearer JWT, populates SecurityContext
            // 3. InternalServiceKeyFilter - /internal/** only; UNCONDITIONALLY OVERWRITES SecurityContext
            .addFilterBefore(correlationIdFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtAuthenticationFilter(), CorrelationIdFilter.class)
            .addFilterAfter(internalServiceKeyFilter(), JwtAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
