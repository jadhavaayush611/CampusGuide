package com.campusguide.platform.auth.service;

import com.campusguide.common.exception.ConflictException;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.platform.auth.dto.AuthResponse;
import com.campusguide.platform.auth.dto.LoginRequest;
import com.campusguide.platform.auth.dto.RegisterRequest;
import com.campusguide.platform.jwt.JwtService;
import com.campusguide.platform.user.entity.UserRole;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@campusguide.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@campusguide.com")
                .password("password123")
                .build();

        user = User.builder()
                .email("test@campusguide.com")
                .username("test")
                .passwordHash("hashedPassword123")
                .role(UserRole.STUDENT)
                .enabled(true)
                .emailVerified(false)
                .build();
    }

    @Test
    void register_Successful() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mocked-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals(registerRequest.getEmail(), response.getEmail());
        assertEquals(UserRole.STUDENT, response.getRole());

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void register_ThrowsConflictException_WhenEmailExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(registerRequest));

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Successful() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mocked-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals(loginRequest.getEmail(), response.getEmail());
        assertEquals(UserRole.STUDENT, response.getRole());

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPasswordHash());
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void login_ThrowsResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginRequest));

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_ThrowsUnauthorisedException_WhenPasswordDoesNotMatch() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())).thenReturn(false);

        assertThrows(UnauthorisedException.class, () -> authService.login(loginRequest));

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPasswordHash());
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }
}
