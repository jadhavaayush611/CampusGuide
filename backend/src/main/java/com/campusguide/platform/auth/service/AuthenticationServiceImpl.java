package com.campusguide.platform.auth.service;

import com.campusguide.platform.auth.dto.request.RegisterRequest;
import com.campusguide.platform.auth.dto.response.AuthenticationResponse;
import com.campusguide.platform.auth.exception.EmailAlreadyExistsException;
import com.campusguide.platform.auth.exception.UsernameAlreadyExistsException;
import com.campusguide.platform.auth.mapper.UserMapper;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = userMapper.toEntity(request, passwordHash);

        userRepository.save(user);

        return AuthenticationResponse.builder()
                .accessToken(null)
                .refreshToken(null)
                .tokenType(null)
                .expiresIn(null)
                .build();
    }
}
