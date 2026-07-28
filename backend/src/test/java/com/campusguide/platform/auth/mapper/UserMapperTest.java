package com.campusguide.platform.auth.mapper;

import com.campusguide.platform.auth.dto.request.RegisterRequest;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toEntity_ShouldMapRegisterRequestToUserEntity() {
        RegisterRequest request = RegisterRequest.builder()
                .email("student@campusguide.com")
                .username("student123")
                .password("Password123!")
                .build();

        String hashedPassword = "encodedPassword123";

        User user = userMapper.toEntity(request, hashedPassword);

        assertNotNull(user);
        assertEquals("student@campusguide.com", user.getEmail());
        assertEquals("student123", user.getUsername());
        assertEquals("encodedPassword123", user.getPasswordHash());
        assertEquals(Role.STUDENT, user.getRole());
        assertTrue(user.isEnabled());
        assertFalse(user.isEmailVerified());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenRequestIsNull() {
        User user = userMapper.toEntity(null, "encodedPassword");

        assertNull(user);
    }
}
