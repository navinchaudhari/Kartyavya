package com.kartyavya.access.service;

import com.kartyavya.access.dto.PageResponse;
import com.kartyavya.access.dto.UserResponse;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.entity.UserRole;
import com.kartyavya.access.exception.PageValidationException;
import com.kartyavya.access.exception.ResourceNotFoundException;
import com.kartyavya.access.exception.SelfActionNotAllowedException;
import com.kartyavya.access.repository.UserRepository;
import com.kartyavya.access.repository.UserRoleRepository;
import com.kartyavya.access.util.PageValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAdminService {

    private static final Set<String> VALID_ROLES = Set.of("CITIZEN", "DEPARTMENT_OFFICER", "ADMIN");

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public UserAdminService(UserRepository userRepository,
                            UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(String role, Boolean enabled, int page, int size) {
        PageValidator.validate(page, size);

        if (role != null && !VALID_ROLES.contains(role)) {
            throw new PageValidationException(Map.of("role", "must be one of CITIZEN, DEPARTMENT_OFFICER, ADMIN"));
        }

        Page<User> userPage = userRepository.findByFilters(
                role, enabled, PageRequest.of(page, size, Sort.by("id").ascending()));

        List<UserResponse> content = userPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );
    }

    @Transactional
    public UserResponse updateStatus(Long id, Boolean enabled, Long currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (id.equals(currentUserId) && Boolean.FALSE.equals(enabled)) {
            throw new SelfActionNotAllowedException("Admin cannot disable their own account.");
        }

        user.setEnabled(enabled);
        User updated = userRepository.save(user);

        return mapToResponse(updated);
    }

    private UserResponse mapToResponse(User user) {
        String roleName = userRoleRepository.findByUser(user)
                .map(ur -> ur.getRole().getName())
                .orElse("UNKNOWN");

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                roleName,
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
