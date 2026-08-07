package com.kartyavya.access.config;

import com.kartyavya.access.security.InternalKeyFilter;
import com.kartyavya.access.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	SecurityFilterChain filter(HttpSecurity http, JwtAuthenticationFilter jwt, InternalKeyFilter internal)
			throws Exception {
		return http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**", "/actuator/health", "/actuator/health/**", "/v3/api-docs/**",
								"/swagger-ui/**", "/swagger-ui.html", "/internal/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/departments/**").permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/citizen/**").hasRole("CITIZEN")
						.anyRequest().authenticated())
				.addFilterBefore(internal, UsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(jwt, InternalKeyFilter.class).build();
	}
}
