package com.kartyavya.access.repository;

import com.kartyavya.access.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);

	long countByRoleAndEnabledTrue(String role);

	List<User> findByRoleOrderByFullNameAsc(String role);
}
