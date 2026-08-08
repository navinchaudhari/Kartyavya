package com.kartyavya.access.service;

import com.kartyavya.access.dto.PageResponse;
import com.kartyavya.access.dto.UserResponse;
import com.kartyavya.access.entity.Role;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.entity.UserRole;
import com.kartyavya.access.exception.PageValidationException;
import com.kartyavya.access.exception.ResourceNotFoundException;
import com.kartyavya.access.exception.SelfActionNotAllowedException;
import com.kartyavya.access.repository.UserRepository;
import com.kartyavya.access.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @InjectMocks private UserAdminService userAdminService;

    @Test
    void list_invalidRole_throwsPageValidationException() {
        assertThrows(PageValidationException.class, () -> userAdminService.list("INVALID", null, 0, 20));
    }

    @Test
    void list_pageNegative_throwsPageValidationException() {
        assertThrows(PageValidationException.class, () -> userAdminService.list(null, null, -1, 20));
    }

    @Test
    void list_sizeTooLarge_throwsPageValidationException() {
        assertThrows(PageValidationException.class, () -> userAdminService.list(null, null, 0, 200));
    }

    @Test
    void list_success_returnsPageResponse() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@test.com");
        
        Role role = new Role();
        role.setName("CITIZEN");
        UserRole ur = new UserRole();
        ur.setRole(role);
        
        when(userRepository.findByFilters(eq("CITIZEN"), eq(true), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(userRoleRepository.findByUser(user)).thenReturn(Optional.of(ur));

        PageResponse<UserResponse> resp = userAdminService.list("CITIZEN", true, 0, 20);
        assertThat(resp.content()).hasSize(1);
        assertThat(resp.content().get(0).role()).isEqualTo("CITIZEN");
    }

    @Test
    void updateStatus_selfDisable_throwsSelfActionNotAllowedException() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(SelfActionNotAllowedException.class,
                () -> userAdminService.updateStatus(1L, false, 1L));
    }

    @Test
    void updateStatus_success_updatesAndReturns() {
        User user = new User();
        user.setId(2L);
        user.setEnabled(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse resp = userAdminService.updateStatus(2L, false, 1L); // current user is 1, target is 2
        assertThat(resp.enabled()).isFalse();
    }
}
