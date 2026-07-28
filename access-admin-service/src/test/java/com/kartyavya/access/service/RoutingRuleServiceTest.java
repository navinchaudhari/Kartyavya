package com.kartyavya.access.service;

import com.kartyavya.access.dto.DepartmentRoutingResponse;
import com.kartyavya.access.dto.RoutingRuleCreateRequest;
import com.kartyavya.access.dto.RoutingRuleResponse;
import com.kartyavya.access.entity.Department;
import com.kartyavya.access.entity.RoutingRule;
import com.kartyavya.access.exception.DepartmentDisabledException;
import com.kartyavya.access.exception.DuplicateResourceException;
import com.kartyavya.access.exception.InvalidCategoryException;
import com.kartyavya.access.exception.RoutingRuleNotFoundException;
import com.kartyavya.access.repository.DepartmentRepository;
import com.kartyavya.access.repository.RoutingRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingRuleServiceTest {

    @Mock private RoutingRuleRepository routingRuleRepository;
    @Mock private DepartmentRepository departmentRepository;
    @InjectMocks private RoutingRuleService routingRuleService;

    @Test
    void create_invalidCategory_throwsInvalidCategoryException() {
        assertThrows(InvalidCategoryException.class,
                () -> routingRuleService.create(new RoutingRuleCreateRequest("INVALID", 1L, true)));
    }

    @Test
    void create_disabledDepartment_throwsDepartmentDisabledException() {
        Department dept = new Department();
        dept.setEnabled(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));

        assertThrows(DepartmentDisabledException.class,
                () -> routingRuleService.create(new RoutingRuleCreateRequest("POTHOLE", 1L, true)));
    }

    @Test
    void create_success_returnsResponse() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setEnabled(true);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(routingRuleRepository.existsByCategory("POTHOLE")).thenReturn(false);

        RoutingRule saved = new RoutingRule();
        saved.setId(10L);
        saved.setCategory("POTHOLE");
        saved.setDepartment(dept);
        saved.setActive(true);
        when(routingRuleRepository.save(any(RoutingRule.class))).thenReturn(saved);

        RoutingRuleResponse resp = routingRuleService.create(new RoutingRuleCreateRequest("POTHOLE", 1L, null));
        assertThat(resp.active()).isTrue(); // defaults to true
    }

    @Test
    void create_fkConstraintRace_throws500() {
        Department dept = new Department();
        dept.setEnabled(true);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(routingRuleRepository.existsByCategory("POTHOLE")).thenReturn(false);
        
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        when(ex.getMostSpecificCause()).thenReturn(new Throwable("fk_routing_department"));
        when(routingRuleRepository.save(any())).thenThrow(ex);

        assertThrows(RuntimeException.class,
                () -> routingRuleService.create(new RoutingRuleCreateRequest("POTHOLE", 1L, true)));
    }

    @Test
    void resolveByCategory_invalidCategory_throwsInvalidCategoryException() {
        assertThrows(InvalidCategoryException.class, () -> routingRuleService.resolveByCategory("INVALID"));
    }

    @Test
    void resolveByCategory_ruleNotFound_throwsRoutingRuleNotFoundException() {
        when(routingRuleRepository.findByCategory("POTHOLE")).thenReturn(Optional.empty());
        assertThrows(RoutingRuleNotFoundException.class, () -> routingRuleService.resolveByCategory("POTHOLE"));
    }

    @Test
    void resolveByCategory_disabledDept_throwsRoutingRuleNotFoundException() {
        Department dept = new Department();
        dept.setEnabled(false);
        RoutingRule rule = new RoutingRule();
        rule.setActive(true);
        rule.setDepartment(dept);
        when(routingRuleRepository.findByCategory("POTHOLE")).thenReturn(Optional.of(rule));

        assertThrows(RoutingRuleNotFoundException.class, () -> routingRuleService.resolveByCategory("POTHOLE"));
    }
}
