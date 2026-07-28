package com.campusguide.platform.auth.service;

import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.platform.auth.dto.AuthResponse;
import com.campusguide.platform.auth.dto.request.LoginRequest;
import com.campusguide.platform.auth.dto.request.RegisterRequest;
import com.campusguide.platform.auth.exception.EmailAlreadyExistsException;
import com.campusguide.platform.auth.exception.UsernameAlreadyExistsException;
import com.campusguide.platform.auth.mapper.UserMapper;
import com.campusguide.platform.jwt.JwtService;
import com.campusguide.platform.user.dto.UserResponse;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        String username = request.getUsername() != null ? request.getUsername()
                : (request.getEmail().contains("@") ? request.getEmail().split("@")[0] : request.getEmail());

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .email(request.getEmail())
                .username(username)
                .passwordHash(passwordHash)
                .role(Role.STUDENT)
                .enabled(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();

        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getEmailOrUsername() != null ? request.getEmailOrUsername() : "";
        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + identifier)));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorisedException("Invalid credentials");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();

        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email)));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
