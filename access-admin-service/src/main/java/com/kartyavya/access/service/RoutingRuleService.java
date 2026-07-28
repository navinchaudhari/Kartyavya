package com.kartyavya.access.service;

import com.kartyavya.access.dto.DepartmentRoutingResponse;
import com.kartyavya.access.dto.RoutingRuleCreateRequest;
import com.kartyavya.access.dto.RoutingRuleResponse;
import com.kartyavya.access.dto.RoutingRuleUpdateRequest;
import com.kartyavya.access.entity.Department;
import com.kartyavya.access.entity.RoutingRule;
import com.kartyavya.access.exception.DepartmentDisabledException;
import com.kartyavya.access.exception.DuplicateResourceException;
import com.kartyavya.access.exception.InvalidCategoryException;
import com.kartyavya.access.exception.ResourceNotFoundException;
import com.kartyavya.access.exception.RoutingRuleNotFoundException;
import com.kartyavya.access.repository.DepartmentRepository;
import com.kartyavya.access.repository.RoutingRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoutingRuleService {

    private static final Logger log = LoggerFactory.getLogger(RoutingRuleService.class);
    private static final Set<String> VALID_CATEGORIES = Set.of(
            "POTHOLE", "GARBAGE", "STREETLIGHT", "WATER_LEAKAGE", "OTHER");

    private final RoutingRuleRepository routingRuleRepository;
    private final DepartmentRepository departmentRepository;

    public RoutingRuleService(RoutingRuleRepository routingRuleRepository,
                              DepartmentRepository departmentRepository) {
        this.routingRuleRepository = routingRuleRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<RoutingRuleResponse> listAll() {
        return routingRuleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoutingRuleResponse create(RoutingRuleCreateRequest req) {
        if (!VALID_CATEGORIES.contains(req.category())) {
            throw new InvalidCategoryException(req.category());
        }

        Department department = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + req.departmentId()));

        if (!department.isEnabled()) {
            throw new DepartmentDisabledException("Cannot create routing rule for a disabled department.");
        }

        if (routingRuleRepository.existsByCategory(req.category())) {
            throw new DuplicateResourceException("Routing rule for this category already exists.");
        }

        RoutingRule rule = new RoutingRule();
        rule.setCategory(req.category());
        rule.setDepartment(department);
        rule.setActive(req.active() == null ? true : req.active());

        try {
            RoutingRule saved = routingRuleRepository.save(rule);
            return mapToResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            String msg = ex.getMostSpecificCause().getMessage();
            if (msg != null && msg.contains("category")) {
                throw new DuplicateResourceException("Routing rule for this category already exists.");
            }
            log.error("Integrity violation during routing rule creation: ", ex);
            throw new RuntimeException("Database error creating routing rule", ex);
        }
    }

    @Transactional
    public RoutingRuleResponse update(Long id, RoutingRuleUpdateRequest req) {
        RoutingRule rule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routing rule not found with id: " + id));

        if (req.departmentId() != null) {
            Department department = departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + req.departmentId()));

            if (!department.isEnabled()) {
                throw new DepartmentDisabledException("Cannot route to a disabled department.");
            }
            rule.setDepartment(department);
        }

        if (req.active() != null) {
            rule.setActive(req.active());
        }

        try {
            RoutingRule updated = routingRuleRepository.save(rule);
            return mapToResponse(updated);
        } catch (DataIntegrityViolationException ex) {
            log.error("Integrity violation during routing rule update: ", ex);
            throw new RuntimeException("Database error updating routing rule", ex);
        }
    }

    @Transactional(readOnly = true)
    public DepartmentRoutingResponse resolveByCategory(String rawCategory) {
        if (rawCategory == null || !VALID_CATEGORIES.contains(rawCategory)) {
            throw new InvalidCategoryException(rawCategory == null ? "null" : rawCategory);
        }

        RoutingRule rule = routingRuleRepository.findByCategory(rawCategory)
                .orElseThrow(() -> new RoutingRuleNotFoundException("No routing rule found for category: " + rawCategory));

        if (!rule.isActive()) {
            throw new RoutingRuleNotFoundException("Routing rule for category " + rawCategory + " is inactive.");
        }

        Department dept = rule.getDepartment();
        if (!dept.isEnabled()) {
            throw new RoutingRuleNotFoundException("Routing rule for category " + rawCategory + " points to a disabled department.");
        }

        return new DepartmentRoutingResponse(
                rule.getCategory(),
                dept.getId(),
                dept.getName(),
                dept.getContactEmail()
        );
    }

    private RoutingRuleResponse mapToResponse(RoutingRule rule) {
        return new RoutingRuleResponse(
                rule.getId(),
                rule.getCategory(),
                rule.getDepartment().getId(),
                rule.isActive()
        );
    }
}
