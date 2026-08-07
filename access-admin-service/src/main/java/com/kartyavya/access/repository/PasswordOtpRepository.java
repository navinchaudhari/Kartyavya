package com.kartyavya.access.repository;

import com.kartyavya.access.entity.PasswordOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.*;

public interface PasswordOtpRepository extends JpaRepository<PasswordOtp, Long> {
	Optional<PasswordOtp> findFirstByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(String email);

	long countByEmailIgnoreCaseAndCreatedAtAfter(String email, Instant after);
}
