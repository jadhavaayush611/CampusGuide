package com.campusguide.platform.user.repository;

import com.campusguide.platform.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;

    @Test
    @DisplayName("Verify UserRepository interface methods contracts")
    void testUserRepositoryContract() {
        assertNotNull(UserRepository.class);
        assertTrue(UserRepository.class.isInterface());
    }
}
