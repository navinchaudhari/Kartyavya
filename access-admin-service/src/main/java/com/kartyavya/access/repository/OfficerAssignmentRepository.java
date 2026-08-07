package com.kartyavya.access.repository;

import com.kartyavya.access.entity.OfficerAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfficerAssignmentRepository extends JpaRepository<OfficerAssignment, Long> {

	@EntityGraph(attributePaths = { "officer", "department" })
	Optional<OfficerAssignment> findByOfficerId(Long officerId);

	@EntityGraph(attributePaths = { "officer", "department" })
	List<OfficerAssignment> findByDepartmentIdAndActiveTrue(Long departmentId);

	@EntityGraph(attributePaths = { "officer", "department" })
	List<OfficerAssignment> findByActiveTrueOrderByOfficerFullNameAsc();

	boolean existsByDepartmentIdAndActiveTrue(Long departmentId);
}
