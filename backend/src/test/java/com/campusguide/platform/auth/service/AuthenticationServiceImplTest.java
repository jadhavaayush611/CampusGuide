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
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("student@campusguide.com")
                .username("student123")
                .password("Password123!")
                .build();

        loginRequest = LoginRequest.builder()
                .emailOrUsername("student@campusguide.com")
                .password("Password123!")
                .build();

        user = User.builder()
                .id("user-123")
                .email("student@campusguide.com")
                .username("student123")
                .passwordHash("encodedPassword123")
                .role(Role.STUDENT)
                .enabled(true)
                .emailVerified(false)
                .build();
    }

    @Test
    void register_ShouldRegisterUserSuccessfully_AndReturnAuthResponseWithToken() {
        when(userRepository.existsByEmail("student@campusguide.com")).thenReturn(false);
        when(userRepository.existsByUsername("student123")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("generated-jwt-token");

        AuthResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertEquals("generated-jwt-token", response.getToken());
        assertEquals("student@campusguide.com", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());

        verify(userRepository).existsByEmail("student@campusguide.com");
        verify(userRepository).existsByUsername("student123");
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void register_ShouldThrowEmailAlreadyExistsException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("student@campusguide.com")).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authenticationService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(userRepository).existsByEmail("student@campusguide.com");
        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    void register_ShouldThrowUsernameAlreadyExistsException_WhenUsernameAlreadyExists() {
        when(userRepository.existsByEmail("student@campusguide.com")).thenReturn(false);
        when(userRepository.existsByUsername("student123")).thenReturn(true);

        UsernameAlreadyExistsException exception = assertThrows(
                UsernameAlreadyExistsException.class,
                () -> authenticationService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("Username already exists"));
        verify(userRepository).existsByEmail("student@campusguide.com");
        verify(userRepository).existsByUsername("student123");
    }

    @Test
    void login_ShouldAuthenticateUser_AndReturnAuthResponse() {
        when(userRepository.findByEmail(loginRequest.getEmailOrUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encodedPassword123")).thenReturn(true);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("generated-jwt-token");

        AuthResponse response = authenticationService.login(loginRequest);

        assertNotNull(response);
        assertEquals("generated-jwt-token", response.getToken());
        assertEquals("student@campusguide.com", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
    }

    @Test
    void login_ShouldThrowUnauthorisedException_WhenPasswordDoesNotMatch() {
        when(userRepository.findByEmail(loginRequest.getEmailOrUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encodedPassword123")).thenReturn(false);

        assertThrows(UnauthorisedException.class, () -> authenticationService.login(loginRequest));
    }

    @Test
    void login_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(userRepository.findByEmail(loginRequest.getEmailOrUsername())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(loginRequest.getEmailOrUsername())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authenticationService.login(loginRequest));
    }

    @Test
    void getCurrentUser_ShouldReturnUserResponse() {
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(user));

        UserResponse response = authenticationService.getCurrentUser("student@campusguide.com");

        assertNotNull(response);
        assertEquals("user-123", response.getId());
        assertEquals("student@campusguide.com", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
    }
}
