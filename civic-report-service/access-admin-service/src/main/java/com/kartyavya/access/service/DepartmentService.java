package com.kartyavya.access.service;

import com.kartyavya.access.dto.DepartmentCreateRequest;
import com.kartyavya.access.dto.DepartmentResponse;
import com.kartyavya.access.dto.DepartmentUpdateRequest;
import com.kartyavya.access.entity.Department;
import com.kartyavya.access.exception.DuplicateResourceException;
import com.kartyavya.access.exception.ResourceNotFoundException;
import com.kartyavya.access.repository.DepartmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listAll() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest req) {
        String name = req.name().strip();

        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Department with this name already exists");
        }

        Department department = new Department();
        department.setName(name);
        department.setContactEmail(req.contactEmail());
        department.setEnabled(req.enabled() == null ? true : req.enabled());

        try {
            Department saved = departmentRepository.save(department);
            return mapToResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            String msg = ex.getMostSpecificCause().getMessage();
            if (msg != null && msg.contains("Duplicate entry") && msg.contains("name")) {
                throw new DuplicateResourceException("Department with this name already exists");
            }
            throw ex;
        }
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentUpdateRequest req) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (req.name() != null) {
            String name = req.name().strip();
            if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new DuplicateResourceException("Department with this name already exists");
            }
            department.setName(name);
        }
        if (req.contactEmail() != null) {
            department.setContactEmail(req.contactEmail());
        }
        if (req.enabled() != null) {
            department.setEnabled(req.enabled());
        }

        try {
            Department updated = departmentRepository.save(department);
            return mapToResponse(updated);
        } catch (DataIntegrityViolationException ex) {
            String msg = ex.getMostSpecificCause().getMessage();
            if (msg != null && msg.contains("Duplicate entry") && msg.contains("name")) {
                throw new DuplicateResourceException("Department with this name already exists");
            }
            throw ex;
        }
    }

    private DepartmentResponse mapToResponse(Department dept) {
        return new DepartmentResponse(dept.getId(), dept.getName(), dept.getContactEmail(), dept.isEnabled());
    }
}
