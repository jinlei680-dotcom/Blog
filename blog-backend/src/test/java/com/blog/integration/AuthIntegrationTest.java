package com.blog.integration;

import com.blog.common.ApiResponse;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.repository.ArticleLikeRepository;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.NotificationRepository;
import com.blog.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleLikeRepository articleLikeRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        articleLikeRepository.deleteAll();
        commentRepository.deleteAll();
        articleRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== Full Auth Flow ==========

    @Test
    @DisplayName("完整认证流程：注册 → 登录 → 获取Token → 使用Token访问认证接口")
    void fullAuthFlow_registerLoginAndAccessProtectedEndpoint() throws Exception {
        // 1. Register
        RegisterRequest registerReq = new RegisterRequest("testuser", "password123", "test@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.id").isNumber());

        // 2. Login and get token
        LoginRequest loginReq = new LoginRequest("testuser", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("testuser"))
                .andReturn();

        // 3. Extract token
        String responseBody = loginResult.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        String token = (String) data.get("token");

        // 4. Access a protected endpoint with valid token (POST /api/articles as an example)
        // This should not return 401 — it may return 400 or other errors due to missing body,
        // but NOT 401, proving the token is accepted.
        mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(not(401)));
    }

    // ========== Registration Tests ==========

    @Test
    @DisplayName("注册成功：返回用户信息且不包含密码")
    void register_success_returnsUserWithoutPassword() throws Exception {
        RegisterRequest req = new RegisterRequest("newuser", "securepass", "new@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("new@example.com"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("重复注册：用户名已存在返回409")
    void register_duplicateUsername_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest("dupuser", "password123", "dup@example.com");
        // First registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Duplicate username registration
        RegisterRequest dupReq = new RegisterRequest("dupuser", "password456", "other@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dupReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    @DisplayName("重复注册：邮箱已存在返回409")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest("user1", "password123", "same@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        RegisterRequest dupReq = new RegisterRequest("user2", "password456", "same@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dupReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    @DisplayName("注册失败：密码过短返回400")
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("shortpw", "12345", "short@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ========== Login Tests ==========

    @Test
    @DisplayName("登录成功：返回JWT Token和用户信息")
    void login_success_returnsTokenAndUser() throws Exception {
        // Register first
        RegisterRequest registerReq = new RegisterRequest("loginuser", "password123", "login@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        // Login
        LoginRequest loginReq = new LoginRequest("loginuser", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("loginuser"))
                .andExpect(jsonPath("$.data.user.email").value("login@example.com"))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
    }

    @Test
    @DisplayName("登录失败：用户不存在返回401")
    void login_userNotFound_returns401() throws Exception {
        LoginRequest req = new LoginRequest("nonexistent", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("登录失败：密码错误返回401")
    void login_wrongPassword_returns401() throws Exception {
        // Register first
        RegisterRequest registerReq = new RegisterRequest("wrongpw", "correctpass", "wrongpw@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        // Login with wrong password
        LoginRequest loginReq = new LoginRequest("wrongpw", "incorrectpass");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    // ========== Token Validation Tests ==========

    @Test
    @DisplayName("无Token访问认证接口返回401")
    void accessProtectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("无效Token访问认证接口返回401")
    void accessProtectedEndpoint_invalidToken_returns401() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("格式错误的Authorization头访问认证接口返回401")
    void accessProtectedEndpoint_malformedAuthHeader_returns401() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .header("Authorization", "NotBearer sometoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
