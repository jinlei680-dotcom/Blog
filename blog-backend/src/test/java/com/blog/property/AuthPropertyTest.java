package com.blog.property;

import com.blog.security.JwtUtil;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature: blog-system
 * 后端认证模块属性测试
 */
class AuthPropertyTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "TestSecretKeyForJwtTokenGenerationAndValidation2024");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        return jwtUtil;
    }

    // =========================================================================
    // Feature: blog-system, Property 1: 密码安全性
    // 对于任意有效的注册请求，BCrypt 加密后的密码不等于原文且可验证。
    // Validates: Requirements 1.5, 1.6
    // =========================================================================

    @Property(tries = 100)
    void bcryptEncodedPasswordIsNeverEqualToRawAndCanBeVerified(
            @ForAll @StringLength(min = 6, max = 50) @AlphaChars String password
    ) {
        String encoded = encoder.encode(password);

        // 加密后不等于原文
        assertThat(encoded).isNotEqualTo(password);
        // 加密结果是有效的 BCrypt 哈希（以 $2a$ 开头）
        assertThat(encoded).startsWith("$2a$");
        // 原始密码可以通过 matches 验证
        assertThat(encoder.matches(password, encoded)).isTrue();
    }

    // =========================================================================
    // Feature: blog-system, Property 2: JWT Token 往返一致性
    // 对于任意用户 ID 和用户名，使用 JwtUtil 编码生成 Token 后再解码，
    // 应能还原出相同的用户 ID 和用户名。
    // Validates: Requirements 2.4
    // =========================================================================

    @Property(tries = 100)
    void jwtTokenRoundTripPreservesUserIdAndUsername(
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) long userId,
            @ForAll @StringLength(min = 1, max = 50) @AlphaChars String username
    ) {
        JwtUtil jwtUtil = createJwtUtil();

        String token = jwtUtil.generateToken(userId, username, "ROLE_USER");

        // Token 不为空
        assertThat(token).isNotNull().isNotEmpty();
        // 解码后用户 ID 一致
        assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo(userId);
        // 解码后用户名一致
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo(username);
        // 解码后角色一致
        assertThat(jwtUtil.getRoleFromToken(token)).isEqualTo("ROLE_USER");
        // Token 有效
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }
}
