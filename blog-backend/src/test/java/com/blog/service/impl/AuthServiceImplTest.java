package com.blog.service.impl;

import com.blog.dto.LoginRequest;
import com.blog.dto.LoginResponse;
import com.blog.dto.RegisterRequest;
import com.blog.dto.UserDTO;
import com.blog.entity.User;
import com.blog.repository.UserRepository;
import com.blog.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "password123", "test@example.com");
        loginRequest = new LoginRequest("testuser", "password123");

        savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("$2a$10$encodedPassword")
                .email("test@example.com")
                .role("ROLE_USER")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ========== 注册成功场景 ==========

    @Test
    void register_success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertNotNull(result.getCreatedAt());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    // ========== 注册失败场景 ==========

    @Test
    void register_duplicateUsername_throwsDuplicateKeyException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        DuplicateKeyException ex = assertThrows(DuplicateKeyException.class,
                () -> authService.register(registerRequest));
        assertEquals("用户名已被占用", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsDuplicateKeyException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        DuplicateKeyException ex = assertThrows(DuplicateKeyException.class,
                () -> authService.register(registerRequest));
        assertEquals("邮箱已被注册", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shortPassword_throwsIllegalArgumentException() {
        RegisterRequest shortPwdRequest = new RegisterRequest("testuser", "12345", "test@example.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(shortPwdRequest));
        assertEquals("密码长度不能少于6个字符", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_nullPassword_throwsIllegalArgumentException() {
        RegisterRequest nullPwdRequest = new RegisterRequest("testuser", null, "test@example.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(nullPwdRequest));
        assertEquals("密码长度不能少于6个字符", ex.getMessage());
    }

    @Test
    void register_passwordEncodedBeforeSave() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("$2a$10$encodedPassword", user.getPassword());
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });

        authService.register(registerRequest);

        verify(passwordEncoder).encode("password123");
    }

    // ========== 登录成功场景 ==========

    @Test
    void login_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser", "ROLE_USER")).thenReturn("jwt-token-value");

        LoginResponse result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("jwt-token-value", result.getToken());
        assertNotNull(result.getUser());
        assertEquals(1L, result.getUser().getId());
        assertEquals("testuser", result.getUser().getUsername());
        assertEquals("test@example.com", result.getUser().getEmail());
        assertEquals("ROLE_USER", result.getRole());
        assertEquals("ROLE_USER", result.getUser().getRole());

        verify(jwtUtil).generateToken(1L, "testuser", "ROLE_USER");
    }

    // ========== 登录失败场景 ==========

    @Test
    void login_userNotFound_throwsBadCredentialsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void login_wrongPassword_throwsBadCredentialsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest));
        assertEquals("密码错误", ex.getMessage());

        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }
}
