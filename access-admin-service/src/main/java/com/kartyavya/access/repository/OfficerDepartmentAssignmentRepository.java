package com.kartyavya.access.repository;

import com.kartyavya.access.entity.OfficerDepartmentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfficerDepartmentAssignmentRepository extends JpaRepository<OfficerDepartmentAssignment, Long> {
    Optional<OfficerDepartmentAssignment> findByOfficerId(Long officerId);
}
