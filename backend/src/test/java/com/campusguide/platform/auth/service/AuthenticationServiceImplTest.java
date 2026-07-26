package com.campusguide.platform.auth.service;

import com.campusguide.platform.auth.dto.request.RegisterRequest;
import com.campusguide.platform.auth.dto.response.AuthenticationResponse;
import com.campusguide.platform.auth.exception.EmailAlreadyExistsException;
import com.campusguide.platform.auth.exception.UsernameAlreadyExistsException;
import com.campusguide.platform.auth.mapper.UserMapper;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.entity.UserRole;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("student@campusguide.com")
                .username("student123")
                .password("Password123!")
                .build();

        user = User.builder()
                .id("user-123")
                .email("student@campusguide.com")
                .username("student123")
                .passwordHash("encodedPassword123")
                .role(UserRole.STUDENT)
                .enabled(true)
                .emailVerified(false)
                .build();
    }

    @Test
    void register_ShouldRegisterUserSuccessfully_AndReturnTransportSafeResponse() {
        when(userRepository.existsByEmail("student@campusguide.com")).thenReturn(false);
        when(userRepository.existsByUsername("student123")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword123");
        when(userMapper.toEntity(registerRequest, "encodedPassword123")).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
        assertNull(response.getTokenType());
        assertNull(response.getExpiresIn());

        verify(userRepository).existsByEmail("student@campusguide.com");
        verify(userRepository).existsByUsername("student123");
        verify(passwordEncoder).encode("Password123!");
        verify(userMapper).toEntity(registerRequest, "encodedPassword123");
        verify(userRepository).save(user);
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
        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).toEntity(any(), anyString());
        verify(userRepository, never()).save(any());
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
        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).toEntity(any(), anyString());
        verify(userRepository, never()).save(any());
    }
}
