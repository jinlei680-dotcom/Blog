package com.blog.service;

import com.blog.dto.LoginRequest;
import com.blog.dto.LoginResponse;
import com.blog.dto.RegisterRequest;
import com.blog.dto.UserDTO;

public interface AuthService {

    UserDTO register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
