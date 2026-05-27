package com.enterprise.news.service;

import com.enterprise.news.dto.auth.AuthResponse;
import com.enterprise.news.dto.auth.LoginRequest;
import com.enterprise.news.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse me();
}