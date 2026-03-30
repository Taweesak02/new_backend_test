package com.test.backend.auth;

import com.test.backend.BackendApplication;
import com.test.backend.dto.Request.LoginRequest;
import com.test.backend.dto.Request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;   // ← เพิ่ม

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    // ✅ Case 1 — register success
    @Test
    void register_success() throws Exception {
        RegisterRequest req = new RegisterRequest("Test User", "newuser@test.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    // ✅ Case 2 — register with existing email
    @Test
    void register_emailAlreadyExists() throws Exception {
        RegisterRequest req = new RegisterRequest("Test User", "admin@test.com", "password123");

        // Step 1: first registration should succeed
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    // ✅ Case 3 — register with invalid email format
    @Test
    void register_invalidEmail() throws Exception {
        RegisterRequest req = new RegisterRequest("Test User", "notanemail", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").exists());
    }

    // ✅ Case 4 — register with short password
    @Test
    void register_shortPassword() throws Exception {
        RegisterRequest req = new RegisterRequest("Test User", "test@test.com", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    // ✅ Case 5 — login success
    @Test
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest("admin@example.com", "password");

        RegisterRequest register = new RegisterRequest("Admin", "admin@example.com", "password");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    // ✅ Case 6 — login wrong password
    @Test
    void login_wrongPassword() throws Exception {
        LoginRequest req = new LoginRequest("admin@example.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ✅ Case 7 — login email not found
    @Test
    void login_emailNotFound() throws Exception {
        LoginRequest req = new LoginRequest("notfound@test.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ✅ Case 8 — access protected route without token
    @Test
    void protectedRoute_noToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    // ✅ Case 9 — access protected route with invalid token
    @Test
    void protectedRoute_invalidToken() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer invalidtoken123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}