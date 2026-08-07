package com.kartyavya.access.security;

import com.kartyavya.access.dto.AuthDtos.LoginResponse;
import com.kartyavya.access.entity.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@Service
public class JwtService {
	@Value("${security.jwt.secret}")
	String secret;
	@Value("${security.jwt.issuer}")
	String issuer;
	@Value("${security.jwt.audience}")
	String audience;
	@Value("${security.jwt.expiry-minutes:60}")
	long expiry;

	public LoginResponse create(User u, OfficerAssignment a) {
		Instant exp = Instant.now().plusSeconds(expiry * 60);
		Map<String, Object> claims = new HashMap<>();
		claims.put("email", u.getEmail());
		claims.put("role", u.getRole());
		if (a != null)
			claims.put("departmentId", a.getDepartment().getId());
		String t = Jwts.builder().claims(claims)
				.subject(u.getId().toString())
				.issuer(issuer)
				.audience().add(audience)
				.and().issuedAt(Date.from(Instant.now()))
				.expiration(Date.from(exp)).id(UUID.randomUUID().toString())
				.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
		return new LoginResponse(t, exp, u.getId(), u.getFullName(), u.getEmail(), u.getRole(),
				a == null ? null : a.getDepartment().getId(), a == null ? null : a.getDepartment().getName());
	}

	public Claims parse(String token) {
		return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
				.requireIssuer(issuer).requireAudience(audience).build().parseSignedClaims(token).getPayload();
	}
}
