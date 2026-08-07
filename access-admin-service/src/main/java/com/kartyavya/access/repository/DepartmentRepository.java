package com.kartyavya.access.repository;

import com.kartyavya.access.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
	boolean existsByNameIgnoreCase(String name);

	List<Department> findByEnabledTrueOrderByNameAsc();

	long countByEnabledTrue();
}
