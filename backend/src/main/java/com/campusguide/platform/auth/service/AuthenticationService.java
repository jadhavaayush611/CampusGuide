package com.campusguide.platform.auth.service;

import com.campusguide.platform.auth.dto.AuthResponse;
import com.campusguide.platform.auth.dto.request.LoginRequest;
import com.campusguide.platform.auth.dto.request.RegisterRequest;
import com.campusguide.platform.user.dto.UserResponse;

public interface AuthenticationService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(String email);
}
