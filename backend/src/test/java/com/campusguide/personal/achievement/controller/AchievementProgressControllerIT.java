package com.campusguide.personal.achievement.controller;

import com.campusguide.personal.achievement.dto.CreateAchievementRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementProgressRequest;
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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AchievementProgressControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AchievementProgressRepository achievementRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User userEntity;
    private UserDetails userDetails;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        achievementRepository.deleteAll();
        userRepository.deleteAll();

        userId = UUID.randomUUID();

        userEntity = User.builder()
                .id(userId.toString())
                .email("student@achievement.com")
                .username("achiever")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userEntity = userRepository.save(userEntity);

        userDetails = org.springframework.security.core.userdetails.User.withUsername("student@achievement.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();
    }

    @AfterEach
    void tearDown() {
        achievementRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateAchievement_Success() throws Exception {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .achievementCode("CODE_FIRST_A")
                .title("First Straight A")
                .description("Earned an A in all courses")
                .category(AchievementCategory.ACADEMIC)
                .progress(25)
                .evidenceUrl("https://example.com/transcript.pdf")
                .metadata(Map.of("term", "Fall 2026"))
                .build();

        mockMvc.perform(post("/api/v1/achievements")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.achievementCode").value("CODE_FIRST_A"))
                .andExpect(jsonPath("$.title").value("First Straight A"))
                .andExpect(jsonPath("$.category").value("ACADEMIC"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.progress").value(25))
                .andExpect(jsonPath("$.evidenceUrl").value("https://example.com/transcript.pdf"))
                .andExpect(jsonPath("$.metadata.term").value("Fall 2026"));
    }

    @Test
    void testGetAchievements_Success() throws Exception {
        AchievementProgress achievement = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userEntity.getId()))
                .achievementCode("CODE_CAMPUS_RUN")
                .title("5K Campus Run")
                .category(AchievementCategory.CAMPUS_LIFE)
                .status(AchievementStatus.IN_PROGRESS)
                .progress(40)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        achievementRepository.save(achievement);

        mockMvc.perform(get("/api/v1/achievements")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].achievementCode").value("CODE_CAMPUS_RUN"));
    }

    @Test
    void testGetAchievementById_Success() throws Exception {
        AchievementProgress achievement = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userEntity.getId()))
                .achievementCode("CODE_SINGLE")
                .title("Single Achievement")
                .category(AchievementCategory.PERSONAL)
                .status(AchievementStatus.LOCKED)
                .progress(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        achievement = achievementRepository.save(achievement);

        mockMvc.perform(get("/api/v1/achievements/" + achievement.getId())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(achievement.getId().toString()))
                .andExpect(jsonPath("$.achievementCode").value("CODE_SINGLE"));
    }

    @Test
    void testUpdateAchievement_Success() throws Exception {
        AchievementProgress achievement = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userEntity.getId()))
                .achievementCode("CODE_UPD")
                .title("Initial Title")
                .category(AchievementCategory.SKILLS)
                .status(AchievementStatus.IN_PROGRESS)
                .progress(50)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        achievement = achievementRepository.save(achievement);

        UpdateAchievementRequest request = UpdateAchievementRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .category(AchievementCategory.SKILLS)
                .progress(75)
                .build();

        mockMvc.perform(put("/api/v1/achievements/" + achievement.getId())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.progress").value(75));
    }

    @Test
    void testPatchAchievementProgress_Success() throws Exception {
        AchievementProgress achievement = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userEntity.getId()))
                .achievementCode("CODE_PATCH")
                .title("Patch Test")
                .category(AchievementCategory.CAREER)
                .status(AchievementStatus.IN_PROGRESS)
                .progress(80)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        achievement = achievementRepository.save(achievement);

        UpdateAchievementProgressRequest request = UpdateAchievementProgressRequest.builder()
                .progress(100)
                .build();

        mockMvc.perform(patch("/api/v1/achievements/" + achievement.getId() + "/progress")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").value(100))
                .andExpect(jsonPath("$.status").value("EARNED"))
                .andExpect(jsonPath("$.earnedAt").exists());
    }

    @Test
    void testDeleteAchievement_Success() throws Exception {
        AchievementProgress achievement = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userEntity.getId()))
                .achievementCode("CODE_DEL")
                .title("Delete Me")
                .category(AchievementCategory.GENERAL)
                .status(AchievementStatus.LOCKED)
                .progress(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        achievement = achievementRepository.save(achievement);

        mockMvc.perform(delete("/api/v1/achievements/" + achievement.getId())
                        .with(user(userDetails)))
                .andExpect(status().isNoContent());

        assertFalse(achievementRepository.findById(achievement.getId()).isPresent());
    }
}
