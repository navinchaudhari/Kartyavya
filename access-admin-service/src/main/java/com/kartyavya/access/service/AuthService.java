package com.kartyavya.access.service;

import com.kartyavya.access.dto.*;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.entity.UserRole;
import com.kartyavya.access.exception.DuplicateResourceException;
import com.kartyavya.access.exception.InvalidCredentialsException;
import com.kartyavya.access.repository.*;
import com.kartyavya.access.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Business logic for authentication and user profile.
 *
 * register:    uniqueness check → BCrypt hash → save User → assign CITIZEN role
 * login:       email lookup → BCrypt verify → load role+dept → issue JWT
 * getProfile:  load user → resolve role+dept from DB (never from request params)
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final OfficerDepartmentAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       OfficerDepartmentAssignmentRepository assignmentRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Register a new CITIZEN user. Throws DuplicateResourceException if email already exists. */
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email address is already registered: " + req.email());
        }

        User user = new User();
        user.setName(req.name());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setEnabled(true);
        User saved = userRepository.save(user);

        var citizenRole = roleRepository.findByName("CITIZEN")
            .orElseThrow(() -> new IllegalStateException(
                "CITIZEN role is not seeded in the database. Verify V1 migration applied correctly."));

        UserRole userRole = new UserRole();
        userRole.setUser(saved);
        userRole.setRole(citizenRole);
        userRoleRepository.save(userRole);

        return new UserResponse(
            saved.getId(), saved.getName(), saved.getEmail(),
            "CITIZEN", saved.isEnabled(), saved.getCreatedAt()
        );
    }

    /** Authenticate and issue a JWT. Throws InvalidCredentialsException on any mismatch. */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        UserRole userRole = userRoleRepository.findByUser(user)
            .orElseThrow(() -> new IllegalStateException(
                "No role assigned to user id=" + user.getId() + ". Data integrity issue."));
        String role = userRole.getRole().getName();

        Long departmentId = null;
        if ("DEPARTMENT_OFFICER".equals(role)) {
            departmentId = assignmentRepository.findByOfficerId(user.getId())
                .filter(OfficerDepartmentAssignment -> OfficerDepartmentAssignment.isActive())
                .map(a -> a.getDepartment().getId())
                .orElse(null);
        }

        String token = jwtService.generateToken(user, role, departmentId);
        Instant expiresAt = Instant.now().plus(jwtService.getExpiryMinutes(), ChronoUnit.MINUTES);

        return new LoginResponse(
            token,
            expiresAt,
            new UserSummary(user.getId(), user.getName(), user.getEmail(), role, departmentId)
        );
    }

    /** Build the profile for the currently authenticated user (userId comes from JWT principal, never from request). */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException(
                "Authenticated user not found in DB for id=" + userId +
                ". Token may outlive user account deletion."));

        UserRole userRole = userRoleRepository.findByUser(user)
            .orElseThrow(() -> new IllegalStateException(
                "No role assigned to user id=" + userId + ". Data integrity issue."));
        String role = userRole.getRole().getName();

        Long departmentId = null;
        if ("DEPARTMENT_OFFICER".equals(role)) {
            departmentId = assignmentRepository.findByOfficerId(userId)
                .filter(a -> a.isActive())
                .map(a -> a.getDepartment().getId())
                .orElse(null);
        }

        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), role, departmentId);
    }
}
