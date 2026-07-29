package com.campusguide.personal.ai.atlas.context.service;

import com.campusguide.personal.ai.atlas.context.model.UserContext;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserContextServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserContextService userContextService;

    @BeforeEach
    void setUp() {
        userContextService = new UserContextService(userRepository);
    }

    @Test
    void testGetUserContext_NullRepository_ReturnsDefaults() {
        UserContextService noRepoService = new UserContextService(null);
        AtlasChatRequest request = AtlasChatRequest.builder()
                .contextPlaceholders(Map.of("student_name", "Jane"))
                .build();

        UserContext context = noRepoService.getUserContext("user-1", request);

        assertNotNull(context);
        assertEquals("user-1", context.getUserId());
        assertEquals("Jane", context.getName());
        assertEquals("ACTIVE", context.getStatus());
        assertTrue(context.getSummary().contains("Jane"));
    }

    @Test
    void testGetUserContext_UserFoundInRepository() {
        User user = User.builder()
                .id("user-100")
                .username("alexsmith")
                .email("alex@campus.edu")
                .build();

        when(userRepository.findById("user-100")).thenReturn(Optional.of(user));

        UserContext context = userContextService.getUserContext("user-100", null);

        assertNotNull(context);
        assertEquals("user-100", context.getUserId());
        assertEquals("alexsmith", context.getName());
        assertEquals("alex@campus.edu", context.getEmail());
        assertEquals("ACTIVE", context.getStatus());
    }
}
