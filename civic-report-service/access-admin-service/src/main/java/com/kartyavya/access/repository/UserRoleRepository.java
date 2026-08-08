package com.kartyavya.access.repository;

import com.kartyavya.access.entity.User;
import com.kartyavya.access.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// PK of UserRole is user_id (Long), matching the @MapsId shared-PK mapping.
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByUser(User user);
}
