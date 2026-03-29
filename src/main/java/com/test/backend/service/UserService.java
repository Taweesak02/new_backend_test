package com.test.backend.service;

import com.test.backend.dto.Request.UpdateUserRequest;
import com.test.backend.dto.Response.UserResponse;
import com.test.backend.entity.Role;
import com.test.backend.entity.User;
import com.test.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> getAllUsers(int page, int limit, String search,
                                           String role, String sort, String order) {

        // Map snake_case request params to camelCase entity field names
        Map<String, String> sortFieldMap = Map.of(
                "created_at", "createdAt",
                "updated_at", "updatedAt",
                "name", "name",
                "email", "email"
        );

        String resolvedSort = sortFieldMap.getOrDefault(sort, "createdAt"); // fallback to createdAt


        Sort.Direction direction = order.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));

        Specification<User> spec = Specification.where((root, query, cb) -> null);

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%")
            ));
        }

        if (role != null && !role.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("role"), Role.valueOf(role.toUpperCase())));
        }

        Page<User> userPage = userRepository.findAll(spec, pageable);

        return Map.of(
                "users", userPage.getContent().stream().map(UserResponse::from).toList(),
                "pagination", Map.of(
                        "currentPage", page,
                        "totalPages", userPage.getTotalPages(),
                        "totalItems", userPage.getTotalElements(),
                        "itemsPerPage", limit
                )
        );
    }

    public UserResponse getUserById(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.from(user);
    }

    public UserResponse updateUser(int id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            if (userRepository.existsByEmail(request.getEmail())
                    && !request.getEmail().equals(user.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail().toLowerCase());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    public void deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    public UserResponse toggleStatus(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(!user.getIsActive());
        return UserResponse.from(userRepository.save(user));
    }

    public void checkOwnerOrAdmin(int id, UserDetails userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = user.getEmail().equals(userDetails.getUsername());

        if (!isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }
}
