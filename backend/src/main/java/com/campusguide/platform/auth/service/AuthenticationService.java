package com.campusguide.platform.auth.service;

import com.campusguide.platform.auth.dto.request.RegisterRequest;
import com.campusguide.platform.auth.dto.response.AuthenticationResponse;

public interface AuthenticationService {

    AuthenticationResponse register(RegisterRequest request);
}
