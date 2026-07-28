package com.kartyavya.access.service;

import com.kartyavya.access.dto.CreateOfficerRequest;
import com.kartyavya.access.dto.OfficerResponse;
import com.kartyavya.access.dto.PageResponse;
import com.kartyavya.access.entity.Department;
import com.kartyavya.access.entity.OfficerDepartmentAssignment;
import com.kartyavya.access.entity.Role;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.exception.DepartmentDisabledException;
import com.kartyavya.access.exception.DuplicateResourceException;
import com.kartyavya.access.repository.DepartmentRepository;
import com.kartyavya.access.repository.OfficerDepartmentAssignmentRepository;
import com.kartyavya.access.repository.RoleRepository;
import com.kartyavya.access.repository.UserRepository;
import com.kartyavya.access.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfficerServiceTest {

    @Mock private OfficerDepartmentAssignmentRepository assignmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private OfficerService officerService;

    @Test
    void createOfficer_success_savesToThreeTables() {
        when(userRepository.existsByEmail("officer@kartyavya.local")).thenReturn(false);
        Department dept = new Department();
        dept.setId(1L);
        dept.setEnabled(true);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        
        User savedUser = new User();
        savedUser.setId(5L);
        when(passwordEncoder.encode("pw")).thenReturn("hash");
        when(userRepository.save(any())).thenReturn(savedUser);
        
        when(roleRepository.findByName("DEPARTMENT_OFFICER")).thenReturn(Optional.of(new Role()));

        officerService.createOfficer(new CreateOfficerRequest("Name", "Officer@Kartyavya.local", "pw", 1L));

        verify(userRepository).save(any());
        verify(userRoleRepository).save(any());
        verify(assignmentRepository).save(any());
    }

    @Test
    void createOfficer_duplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.existsByEmail("officer@kartyavya.local")).thenReturn(true);
        assertThrows(DuplicateResourceException.class,
                () -> officerService.createOfficer(new CreateOfficerRequest("Name", "Officer@Kartyavya.local", "pw", 1L)));
    }

    @Test
    void createOfficer_disabledDept_throwsDepartmentDisabledException() {
        Department dept = new Department();
        dept.setEnabled(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));

        assertThrows(DepartmentDisabledException.class,
                () -> officerService.createOfficer(new CreateOfficerRequest("Name", "officer@kartyavya.local", "pw", 1L)));
    }

    @Test
    void list_success_resolvesOfficerDetails() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("Roads");

        OfficerDepartmentAssignment assignment = new OfficerDepartmentAssignment();
        assignment.setOfficerId(5L);
        assignment.setDepartment(dept);

        User user = new User();
        user.setId(5L);
        user.setName("Officer");

        when(assignmentRepository.findByActiveTrue(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(assignment)));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        PageResponse<OfficerResponse> resp = officerService.list(0, 20);
        assertThat(resp.content()).hasSize(1);
        assertThat(resp.content().get(0).name()).isEqualTo("Officer");
        assertThat(resp.content().get(0).departmentName()).isEqualTo("Roads");
    }
}
