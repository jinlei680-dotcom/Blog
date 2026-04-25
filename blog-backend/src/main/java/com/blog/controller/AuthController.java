package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.LoginRequest;
import com.blog.dto.LoginResponse;
import com.blog.dto.RegisterRequest;
import com.blog.dto.UserDTO;
import com.blog.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@RequestBody RegisterRequest request) {
        UserDTO user = authService.register(request);
        return ApiResponse.success("注册成功", user);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ApiResponse.success("登录成功", loginResponse);
    }
}
