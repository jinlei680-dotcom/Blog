package com.blog.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "TestSecretKeyForJwtTokenGenerationAndValidation2024");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    // ========== Token 生成 ==========

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken(1L, "testuser", "ROLE_USER");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_differentUsersProduceDifferentTokens() {
        String token1 = jwtUtil.generateToken(1L, "user1", "ROLE_USER");
        String token2 = jwtUtil.generateToken(2L, "user2", "ROLE_ADMIN");
        assertNotEquals(token1, token2);
    }

    // ========== Token 解析 ==========

    @Test
    void getUserIdFromToken_returnsCorrectUserId() {
        String token = jwtUtil.generateToken(42L, "testuser", "ROLE_USER");
        Long userId = jwtUtil.getUserIdFromToken(token);
        assertEquals(42L, userId);
    }

    @Test
    void getUsernameFromToken_returnsCorrectUsername() {
        String token = jwtUtil.generateToken(1L, "myuser", "ROLE_USER");
        String username = jwtUtil.getUsernameFromToken(token);
        assertEquals("myuser", username);
    }

    @Test
    void getRoleFromToken_returnsCorrectRole() {
        String token = jwtUtil.generateToken(1L, "testuser", "ROLE_ADMIN");
        String role = jwtUtil.getRoleFromToken(token);
        assertEquals("ROLE_ADMIN", role);
    }

    @Test
    void tokenRoundTrip_preservesUserIdAndUsernameAndRole() {
        Long originalId = 99L;
        String originalUsername = "roundtripuser";
        String originalRole = "ROLE_USER";

        String token = jwtUtil.generateToken(originalId, originalUsername, originalRole);

        assertEquals(originalId, jwtUtil.getUserIdFromToken(token));
        assertEquals(originalUsername, jwtUtil.getUsernameFromToken(token));
        assertEquals(originalRole, jwtUtil.getRoleFromToken(token));
    }

    // ========== Token 验证 ==========

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken(1L, "testuser", "ROLE_USER");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        assertFalse(jwtUtil.validateToken("not.a.valid.token"));
    }

    @Test
    void validateToken_emptyToken_returnsFalse() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        JwtUtil shortLivedJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortLivedJwtUtil, "secret", "TestSecretKeyForJwtTokenGenerationAndValidation2024");
        ReflectionTestUtils.setField(shortLivedJwtUtil, "expiration", -1000L); // already expired

        String token = shortLivedJwtUtil.generateToken(1L, "testuser", "ROLE_USER");
        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_wrongSecret_returnsFalse() {
        JwtUtil otherJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherJwtUtil, "secret", "DifferentSecretKeyThatDoesNotMatch12345");
        ReflectionTestUtils.setField(otherJwtUtil, "expiration", 86400000L);

        String token = otherJwtUtil.generateToken(1L, "testuser", "ROLE_USER");
        assertFalse(jwtUtil.validateToken(token));
    }

    // ========== 密码加密验证（BCrypt 行为测试） ==========

    @Test
    void bcryptEncoder_encodedPasswordDiffersFromRaw() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String raw = "password123";
        String encoded = encoder.encode(raw);

        assertNotEquals(raw, encoded);
        assertTrue(encoded.startsWith("$2a$"));
    }

    @Test
    void bcryptEncoder_matchesReturnsTrueForCorrectPassword() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String raw = "password123";
        String encoded = encoder.encode(raw);

        assertTrue(encoder.matches(raw, encoded));
    }

    @Test
    void bcryptEncoder_matchesReturnsFalseForWrongPassword() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String encoded = encoder.encode("password123");

        assertFalse(encoder.matches("wrongpassword", encoded));
    }

    @Test
    void bcryptEncoder_samePasswordProducesDifferentHashes() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String raw = "password123";
        String encoded1 = encoder.encode(raw);
        String encoded2 = encoder.encode(raw);

        // BCrypt uses random salt, so same input produces different hashes
        assertNotEquals(encoded1, encoded2);
        // But both should match the original
        assertTrue(encoder.matches(raw, encoded1));
        assertTrue(encoder.matches(raw, encoded2));
    }
}
