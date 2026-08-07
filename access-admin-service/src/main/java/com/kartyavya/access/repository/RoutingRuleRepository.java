package com.kartyavya.access.repository;

import com.kartyavya.access.entity.RoutingRule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Long> {

	@EntityGraph(attributePaths = "department")
	Optional<RoutingRule> findByCategoryIgnoreCase(String category);

	@EntityGraph(attributePaths = "department")
	List<RoutingRule> findAllByOrderByCategoryAsc();
}
