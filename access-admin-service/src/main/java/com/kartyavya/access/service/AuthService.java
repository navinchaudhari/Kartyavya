package com.kartyavya.access.service;

import com.kartyavya.access.dto.AuthDtos.*;
import com.kartyavya.access.entity.*;
import com.kartyavya.access.repository.*;
import com.kartyavya.access.security.JwtService;
import com.kartyavya.contracts.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository users;
	private final OfficerAssignmentRepository assignments;
	private final PasswordOtpRepository otps;
	private final PasswordEncoder encoder;
	private final JwtService jwt;
	private final EventPublisher events;
	private final SecureRandom random = new SecureRandom();

	@Transactional
	public void register(RegisterRequest r, String correlation) {
		String email = r.email().trim().toLowerCase(Locale.ROOT);
		if (users.existsByEmailIgnoreCase(email))
			throw new IllegalStateException("Email already registered");
		User u = new User();
		u.setFullName(r.fullName().trim());
		u.setEmail(email);
		u.setPasswordHash(encoder.encode(r.password()));
		u.setMobileNumber(r.mobileNumber());
		u.setAddress(r.address().trim());
		u.setRole("Citizen");
		users.save(u);
		events.publish(EventNames.USER_REGISTERED, correlation,
				new Events.UserRegistered(u.getId(), u.getFullName(), u.getEmail()));
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest r) {
		User u = users.findByEmailIgnoreCase(r.email())
				.orElseThrow(() -> new SecurityException("Invalid email or password"));
		if (!u.isEnabled() || !encoder.matches(r.password(), u.getPasswordHash()))
			throw new SecurityException("Invalid email or password");
		OfficerAssignment a = "Officer".equals(u.getRole()) ? assignments.findByOfficerId(u.getId()).orElse(null)
				: null;
		return jwt.create(u, a);
	}

	@Transactional
	public void forgot(ForgotRequest r, String correlation) {
		String email = r.email().trim().toLowerCase(Locale.ROOT);
		User u = users.findByEmailIgnoreCase(email).orElse(null);
		if (u == null || !u.isEnabled())
			return;
		if (otps.countByEmailIgnoreCaseAndCreatedAtAfter(email, Instant.now().minusSeconds(600)) >= 3)
			throw new IllegalStateException("Too many OTP requests. Try again after 10 minutes.");
		String otp = String.format("%06d", random.nextInt(1_000_000));
		PasswordOtp rec = new PasswordOtp();
		rec.setEmail(email);
		rec.setOtpHash(encoder.encode(otp));
		rec.setExpiresAt(Instant.now().plusSeconds(600));
		otps.save(rec);
		events.publish(EventNames.PASSWORD_OTP_REQUESTED, correlation,
				new Events.PasswordOtpRequested(u.getFullName(), u.getEmail(), otp, rec.getExpiresAt()));
	}

	@Transactional
	public boolean verify(VerifyOtpRequest r) {
		String email = normalizeEmail(r.email());
		PasswordOtp o = latest(email);
		if (o == null)
			return false;
		boolean ok = encoder.matches(r.otp(), o.getOtpHash());
		if (!ok) {
			o.setAttempts(o.getAttempts() + 1);
			otps.save(o);
		}
		return ok;
	}

	@Transactional
	public void reset(ResetPasswordRequest r, String correlation) {
		String email = normalizeEmail(r.email());
		PasswordOtp o = latest(email);
		if (o == null)
			throw new IllegalArgumentException("OTP is invalid or expired");
		if (!encoder.matches(r.otp(), o.getOtpHash())) {
			o.setAttempts(o.getAttempts() + 1);
			otps.save(o);
			throw new IllegalArgumentException("OTP is invalid or expired");
		}
		User u = users.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalArgumentException("OTP is invalid or expired"));
		u.setPasswordHash(encoder.encode(r.newPassword()));
		o.setUsed(true);
		users.save(u);
		otps.save(o);
		events.publish(EventNames.PASSWORD_RESET, correlation, new Events.PasswordReset(u.getFullName(), u.getEmail()));
	}

	private PasswordOtp latest(String email) {
		PasswordOtp o = otps.findFirstByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(normalizeEmail(email))
				.orElse(null);
		return o == null || o.getExpiresAt().isBefore(Instant.now()) || o.getAttempts() >= 5 ? null : o;
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
