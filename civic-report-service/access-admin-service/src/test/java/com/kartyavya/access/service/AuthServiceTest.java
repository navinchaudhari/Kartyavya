package com.kartyavya.access.service;

import com.kartyavya.access.dto.*;
import com.kartyavya.access.entity.Role;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.entity.UserRole;
import com.kartyavya.access.exception.DuplicateResourceException;
import com.kartyavya.access.exception.InvalidCredentialsException;
import com.kartyavya.access.repository.*;
import com.kartyavya.access.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService — no Spring context, no DB, no Config Server.
 * All dependencies are mocked via Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private OfficerDepartmentAssignmentRepository assignmentRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role citizenRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("unit@example.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setEnabled(true);
        testUser.setCreatedAt(Instant.now());

        citizenRole = new Role();
        citizenRole.setId(1L);
        citizenRole.setName("CITIZEN");
    }

    @Test
    void register_success_savesUserAndAssignsCitizenRole() {
        RegisterRequest req = new RegisterRequest("Test User", "unit@example.com", "Test@1234");

        when(userRepository.existsByEmail("unit@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Test@1234")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(roleRepository.findByName("CITIZEN")).thenReturn(Optional.of(citizenRole));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(new UserRole());

        UserResponse result = authService.register(req);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("unit@example.com");
        assertThat(result.role()).isEqualTo("CITIZEN");
        assertThat(result.enabled()).isTrue();
        verify(userRepository).save(any(User.class));
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest req = new RegisterRequest("Test", "unit@example.com", "Test@1234");
        when(userRepository.existsByEmail("unit@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("unit@example.com");

        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void login_success_returnsLoginResponseWithNonNullToken() {
        LoginRequest req = new LoginRequest("unit@example.com", "Test@1234");

        UserRole userRole = new UserRole();
        userRole.setUser(testUser);
        userRole.setRole(citizenRole);

        when(userRepository.findByEmail("unit@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Test@1234", "$2a$10$hashedpassword")).thenReturn(true);
        when(userRoleRepository.findByUser(testUser)).thenReturn(Optional.of(userRole));
        when(jwtService.generateToken(testUser, "CITIZEN", null)).thenReturn("mocked.jwt.token");
        when(jwtService.getExpiryMinutes()).thenReturn(60L);

        LoginResponse result = authService.login(req);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("mocked.jwt.token");
        assertThat(result.user().email()).isEqualTo("unit@example.com");
        assertThat(result.user().role()).isEqualTo("CITIZEN");
        assertThat(result.user().departmentId()).isNull();
        assertThat(result.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        LoginRequest req = new LoginRequest("unit@example.com", "WrongPass@1");
        when(userRepository.findByEmail("unit@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPass@1", "$2a$10$hashedpassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_emailNotFound_throwsInvalidCredentialsException() {
        LoginRequest req = new LoginRequest("nobody@example.com", "Test@1234");
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(InvalidCredentialsException.class);
    }
}
