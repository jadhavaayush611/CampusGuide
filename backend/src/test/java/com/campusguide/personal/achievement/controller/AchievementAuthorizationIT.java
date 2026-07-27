package com.campusguide.personal.achievement.controller;

import com.campusguide.personal.achievement.dto.CreateAchievementRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementRequest;
import com.campusguide.personal.achievement.entity.AchievementCategory;
import com.campusguide.personal.achievement.entity.AchievementProgress;
import com.campusguide.personal.achievement.entity.AchievementStatus;
import com.campusguide.personal.achievement.repository.AchievementProgressRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AchievementAuthorizationIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AchievementProgressRepository achievementRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User user1;
    private User user2;
    private UserDetails userDetails1;
    private UserDetails userDetails2;
    private AchievementProgress user2Achievement;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        achievementRepository.deleteAll();
        userRepository.deleteAll();

        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        user1 = User.builder()
                .id(user1Id.toString())
                .email("user1@achievement.com")
                .username("user1achieve")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .id(user2Id.toString())
                .email("user2@achievement.com")
                .username("user2achieve")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user2 = userRepository.save(user2);

        userDetails1 = org.springframework.security.core.userdetails.User.withUsername("user1@achievement.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        userDetails2 = org.springframework.security.core.userdetails.User.withUsername("user2@achievement.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        user2Achievement = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(user2.getId()))
                .achievementCode("USER2_CODE")
                .title("User 2 Private Achievement")
                .category(AchievementCategory.PERSONAL)
                .status(AchievementStatus.IN_PROGRESS)
                .progress(50)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user2Achievement = achievementRepository.save(user2Achievement);
    }

    @AfterEach
    void tearDown() {
        achievementRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void unauthenticatedRequest_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/achievements"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/achievements/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        CreateAchievementRequest createRequest = CreateAchievementRequest.builder()
                .achievementCode("UNAUTH_CODE")
                .title("Unauth Achievement")
                .category(AchievementCategory.ACADEMIC)
                .build();

        mockMvc.perform(post("/api/v1/achievements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessingOtherUserAchievement_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/achievements/" + user2Achievement.getId())
                        .with(user(userDetails1)))
                .andExpect(status().isForbidden());

        UpdateAchievementRequest updateRequest = UpdateAchievementRequest.builder()
                .title("Hacked Title")
                .category(AchievementCategory.PERSONAL)
                .build();

        mockMvc.perform(put("/api/v1/achievements/" + user2Achievement.getId())
                        .with(user(userDetails1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/achievements/" + user2Achievement.getId())
                        .with(user(userDetails1)))
                .andExpect(status().isForbidden());
    }
}
