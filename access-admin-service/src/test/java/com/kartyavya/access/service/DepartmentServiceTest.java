package com.kartyavya.access.service;

import com.kartyavya.access.dto.DepartmentCreateRequest;
import com.kartyavya.access.dto.DepartmentResponse;
import com.kartyavya.access.dto.DepartmentUpdateRequest;
import com.kartyavya.access.entity.Department;
import com.kartyavya.access.exception.DuplicateResourceException;
import com.kartyavya.access.exception.ResourceNotFoundException;
import com.kartyavya.access.repository.DepartmentRepository;
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
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;
    @InjectMocks private DepartmentService departmentService;

    @Test
    void create_success_returnsResponse() {
        when(departmentRepository.existsByNameIgnoreCase("IT")).thenReturn(false);
        Department saved = new Department();
        saved.setId(1L);
        saved.setName("IT");
        saved.setContactEmail("it@kartyavya.local");
        saved.setEnabled(true);
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);

        DepartmentResponse resp = departmentService.create(new DepartmentCreateRequest("IT", "it@kartyavya.local", null));
        assertThat(resp.name()).isEqualTo("IT");
        assertThat(resp.enabled()).isTrue(); // defaults to true
    }

    @Test
    void create_duplicateNamePrecheck_throwsDuplicateResourceException() {
        when(departmentRepository.existsByNameIgnoreCase("IT")).thenReturn(true);
        assertThrows(DuplicateResourceException.class,
                () -> departmentService.create(new DepartmentCreateRequest("IT", "it@kartyavya.local", null)));
    }

    @Test
    void create_duplicateNameRaceCondition_throwsDuplicateResourceException() {
        when(departmentRepository.existsByNameIgnoreCase("IT")).thenReturn(false);
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Throwable cause = new Throwable("Duplicate entry 'IT' for key 'name'");
        when(ex.getMostSpecificCause()).thenReturn(cause);
        when(departmentRepository.save(any(Department.class))).thenThrow(ex);

        assertThrows(DuplicateResourceException.class,
                () -> departmentService.create(new DepartmentCreateRequest("IT", "it@kartyavya.local", null)));
    }

    @Test
    void update_nonexistentId_throwsResourceNotFoundException() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> departmentService.update(99L, new DepartmentUpdateRequest("New Name", null, null)));
    }

    @Test
    void update_duplicateName_throwsDuplicateResourceException() {
        Department existing = new Department();
        existing.setId(1L);
        existing.setName("Old Name");
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.existsByNameIgnoreCaseAndIdNot("New Name", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> departmentService.update(1L, new DepartmentUpdateRequest("New Name", null, null)));
    }
}
