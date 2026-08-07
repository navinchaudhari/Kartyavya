package com.kartyavya.access.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "password_otps", indexes = @Index(name = "idx_otp_email_created", columnList = "email,created_at"))
@Getter
@Setter
@NoArgsConstructor
public class PasswordOtp {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, length = 190)
	private String email;
	
	@Column(name = "otp_hash", nullable = false, length = 255)
	private String otpHash;
	
	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;
	
	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();
	
	@Column(nullable = false)
	private int attempts;
	
	@Column(nullable = false)
	private boolean used;
}
