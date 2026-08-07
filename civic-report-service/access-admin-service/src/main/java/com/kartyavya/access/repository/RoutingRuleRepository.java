package com.kartyavya.access.repository;

import com.kartyavya.access.entity.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Long> {

    boolean existsByCategory(String category);
    java.util.Optional<RoutingRule> findByCategory(String category);
}
