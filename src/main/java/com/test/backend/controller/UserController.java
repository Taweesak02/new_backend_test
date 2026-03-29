package com.test.backend.controller;

import com.test.backend.dto.Request.UpdateUserRequest;
import com.test.backend.dto.Response.ApiResponse;
import com.test.backend.dto.Response.UserResponse;
import com.test.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@Tag(name = "Users", description = "User management endpoints")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users (Admin only)",
            security = @SecurityRequirement(name = "Bearer Token"))
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String order) {

        Map<String, Object> data = userService.getAllUsers(page, limit, search, role, sort, order);
        return ResponseEntity.ok(ApiResponse.success(data, "OK"));
    }

    @Operation(summary = "Get user by ID",
            security = @SecurityRequirement(name = "Bearer Token"))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable int id,
            @AuthenticationPrincipal UserDetails userDetails) {

        userService.checkOwnerOrAdmin(id, userDetails);
        UserResponse data = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(data, "OK"));
    }

    @Operation(summary = "Change user Data by ID",
            security = @SecurityRequirement(name = "Bearer Token"))
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable int id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        userService.checkOwnerOrAdmin(id, userDetails);
        UserResponse data = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(data, "User updated"));
    }

    @Operation(summary = "Delete user (Admin only)",
            security = @SecurityRequirement(name = "Bearer Token"))
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted"));
    }

    @Operation(summary = "Set Active user by ID",
            security = @SecurityRequirement(name = "Bearer Token"))
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleStatus(@PathVariable int id) {
        UserResponse data = userService.toggleStatus(id);
        return ResponseEntity.ok(ApiResponse.success(data, "Status updated"));
    }
}
