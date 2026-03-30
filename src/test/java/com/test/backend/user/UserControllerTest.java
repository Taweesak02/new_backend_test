package com.test.backend.user;

import com.test.backend.BackendApplication;
import com.test.backend.dto.Request.LoginRequest;
import com.test.backend.dto.Request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.jdbc.core.JdbcTemplate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;               // ← ไม่มี @Autowired
    private String adminToken;
    private String userToken;
    private int userId;

    @BeforeEach
    void setup() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        registerAsAdmin("admin@example.com", "password");
        adminToken = loginAndGetToken("admin@example.com", "password");
        String email = "user" + System.currentTimeMillis() + "@test.com";
        userId = registerAndGetId("Test User", email, "password123");
        userToken = loginAndGetToken(email, "password123");
    }

    // ✅ Case 10 — admin can get all users
    @Test
    void getUsers_adminSuccess() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.users").isArray())        // ← was $.data
                .andExpect(jsonPath("$.data.pagination").exists())
                .andExpect(jsonPath("$.data.pagination.currentPage").value(1))
                .andExpect(jsonPath("$.data.pagination.totalItems").isNumber());
    }

    // ✅ Case 11 — user cannot get all users (forbidden)
    @Test
    void getUsers_userForbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));;
    }

    // ✅ Case 12 — user can get their own profile
    @Test
    void getUserById_ownProfile() throws Exception {

        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ✅ Case 13 — user cannot get another user's profile
    @Test
    void getUserById_otherUserForbidden() throws Exception {
        int otherid = registerAndGetId("Other Test User", "other@example.com", "password123");
        mockMvc.perform(get("/api/users/"+otherid) // admin's id
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ✅ Case 14 — admin can delete user
    @Test
    void deleteUser_adminSuccess() throws Exception {
        mockMvc.perform(delete("/api/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ✅ Case 15 — user cannot delete another user
    @Test
    void deleteUser_userForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── helpers ──────────────────────────────────────────────────
    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest(email, password);
        String res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).path("data").path("accessToken").asText();
    }

    private int registerAndGetId(String name, String email, String password) throws Exception {
        RegisterRequest req = new RegisterRequest(name, email, password);
        String res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        System.out.println("=== register response: " + res);  // check actual path
        return objectMapper.readTree(res).path("data").path("user").path("id").asInt();

    }

    private void registerAsAdmin(String email, String password) {
        jdbcTemplate.update(
                "INSERT INTO users (name, email, password, role, is_active, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
                "Admin", email, passwordEncoder.encode(password), "admin", true
        );
    }
}