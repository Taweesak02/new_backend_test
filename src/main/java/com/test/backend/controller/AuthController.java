package com.test.backend.controller;

import com.test.backend.dto.Request.LoginRequest;
import com.test.backend.dto.Request.RefreshRequest;
import com.test.backend.dto.Request.RegisterRequest;
import com.test.backend.dto.Response.ApiResponse;
import com.test.backend.dto.Response.AuthResponse;
import com.test.backend.dto.Response.UserResponse;
import com.test.backend.service.AuthService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "Authentication endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register new user",responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(examples = @ExampleObject(value = """
                    {"success": true, "data": {"accessToken": "eyJ..."}}
                """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse data = authService.register(request);
        return ResponseEntity
                .status(201)
                .body(ApiResponse.success(data, "Registered successfully"));
    }

    @Operation(summary = "Login",responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(examples = @ExampleObject(value = """
                    {"success": true, "data": {"accessToken": "eyJ...", "refreshToken": "eyJ..."}}
                """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Wrong credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(data, "Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse data = authService.getMe(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(data, "OK"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody RefreshRequest refreshToken) {
        AuthResponse data = authService.refresh(refreshToken.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(data, "Token refreshed"));
    }
}