package com.kartyavya.access.repository;

import com.kartyavya.access.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u JOIN u.userRole ur JOIN ur.role r " +
           "WHERE (:role IS NULL OR r.name = :role) " +
           "AND (:enabled IS NULL OR u.enabled = :enabled)")
    org.springframework.data.domain.Page<User> findByFilters(@org.springframework.data.repository.query.Param("role") String role,
                             @org.springframework.data.repository.query.Param("enabled") Boolean enabled,
                             org.springframework.data.domain.Pageable pageable);
}
