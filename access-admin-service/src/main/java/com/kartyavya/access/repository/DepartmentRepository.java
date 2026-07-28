package com.kartyavya.access.repository;

import com.kartyavya.access.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /** Used by create: check for name collision across all departments. */
    boolean existsByNameIgnoreCase(String name);

    /** Used by update: check for name collision excluding the department being updated. */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
