package com.kartyavya.access.service;

import com.kartyavya.access.dto.CreateOfficerRequest;
import com.kartyavya.access.dto.OfficerResponse;
import com.kartyavya.access.dto.PageResponse;
import com.kartyavya.access.dto.UserResponse;
import com.kartyavya.access.entity.Department;
import com.kartyavya.access.entity.OfficerDepartmentAssignment;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.entity.UserRole;
import com.kartyavya.access.exception.DepartmentDisabledException;
import com.kartyavya.access.exception.DuplicateResourceException;
import com.kartyavya.access.exception.ResourceNotFoundException;
import com.kartyavya.access.repository.DepartmentRepository;
import com.kartyavya.access.repository.OfficerDepartmentAssignmentRepository;
import com.kartyavya.access.repository.RoleRepository;
import com.kartyavya.access.repository.UserRepository;
import com.kartyavya.access.repository.UserRoleRepository;
import com.kartyavya.access.util.EmailNormalizer;
import com.kartyavya.access.util.PageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfficerService {

    private static final Logger log = LoggerFactory.getLogger(OfficerService.class);

    private final OfficerDepartmentAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public OfficerService(OfficerDepartmentAssignmentRepository assignmentRepository,
                          UserRepository userRepository,
                          DepartmentRepository departmentRepository,
                          RoleRepository roleRepository,
                          UserRoleRepository userRoleRepository,
                          PasswordEncoder passwordEncoder) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<OfficerResponse> list(int page, int size) {
        PageValidator.validate(page, size);

        Page<OfficerDepartmentAssignment> assignments = assignmentRepository.findByActiveTrue(
                PageRequest.of(page, size, Sort.by("id").ascending()));

        List<OfficerResponse> content = assignments.getContent().stream()
                .map(a -> {
                    User user = userRepository.findById(a.getOfficerId())
                            .orElseThrow(() -> new ResourceNotFoundException("Officer user not found"));
                    return new OfficerResponse(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            a.getDepartment().getId(),
                            a.getDepartment().getName()
                    );
                })
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                assignments.getNumber(),
                assignments.getSize(),
                assignments.getTotalElements(),
                assignments.getTotalPages(),
                assignments.isLast()
        );
    }

    @Transactional
    public UserResponse createOfficer(CreateOfficerRequest req) {
        String normalizedEmail = EmailNormalizer.normalize(req.email());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email address is already registered: " + req.email());
        }

        Department department = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + req.departmentId()));

        if (!department.isEnabled()) {
            throw new DepartmentDisabledException("Cannot assign an officer to a disabled department.");
        }

        try {
            // 1. Insert User
            User user = new User();
            user.setName(req.name());
            user.setEmail(normalizedEmail);
            user.setPasswordHash(passwordEncoder.encode(req.password()));
            user.setEnabled(true);
            User savedUser = userRepository.save(user);

            // 2. Insert UserRole
            var officerRole = roleRepository.findByName("DEPARTMENT_OFFICER")
                    .orElseThrow(() -> new IllegalStateException("DEPARTMENT_OFFICER role is missing in DB"));

            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(officerRole);
            userRoleRepository.save(userRole);

            // 3. Insert OfficerDepartmentAssignment
            OfficerDepartmentAssignment assignment = new OfficerDepartmentAssignment();
            assignment.setOfficerId(savedUser.getId());
            assignment.setDepartment(department);
            assignment.setActive(true);
            assignmentRepository.save(assignment);

            return new UserResponse(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    "DEPARTMENT_OFFICER",
                    savedUser.isEnabled(),
                    savedUser.getCreatedAt()
            );
        } catch (DataIntegrityViolationException ex) {
            String msg = ex.getMostSpecificCause().getMessage();
            if (msg != null && msg.contains("users") && msg.contains("email")) {
                throw new DuplicateResourceException("Email address is already registered: " + req.email());
            }
            log.error("Integrity violation during officer creation: ", ex);
            throw new RuntimeException("Database error creating officer", ex);
        }
    }
}
