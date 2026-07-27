package com.campusguide.personal.achievement.repository;

import com.campusguide.personal.achievement.entity.AchievementCategory;
import com.campusguide.personal.achievement.entity.AchievementProgress;
import com.campusguide.personal.achievement.entity.AchievementStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AchievementProgressRepositoryIT {

    @Autowired
    private AchievementProgressRepository repository;

    private UUID userId;
    private AchievementProgress achievement1;
    private AchievementProgress achievement2;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        userId = UUID.randomUUID();

        achievement1 = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .achievementCode("CODE_001")
                .title("First Achievement")
                .description("Desc 1")
                .category(AchievementCategory.ACADEMIC)
                .status(AchievementStatus.IN_PROGRESS)
                .progress(50)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        achievement2 = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .achievementCode("CODE_002")
                .title("Second Achievement")
                .description("Desc 2")
                .category(AchievementCategory.CAMPUS_LIFE)
                .status(AchievementStatus.EARNED)
                .progress(100)
                .earnedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        repository.save(achievement1);
        repository.save(achievement2);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void findByUserId_ReturnsUserAchievements() {
        List<AchievementProgress> results = repository.findByUserId(userId);
        assertEquals(2, results.size());
    }

    @Test
    void findByUserIdAndCategory_ReturnsFilteredList() {
        List<AchievementProgress> results = repository.findByUserIdAndCategory(userId, AchievementCategory.ACADEMIC);
        assertEquals(1, results.size());
        assertEquals("CODE_001", results.get(0).getAchievementCode());
    }

    @Test
    void findByUserIdAndStatus_ReturnsFilteredList() {
        List<AchievementProgress> results = repository.findByUserIdAndStatus(userId, AchievementStatus.EARNED);
        assertEquals(1, results.size());
        assertEquals("CODE_002", results.get(0).getAchievementCode());
    }

    @Test
    void findByIdAndUserId_ReturnsAchievement() {
        Optional<AchievementProgress> found = repository.findByIdAndUserId(achievement1.getId(), userId);
        assertTrue(found.isPresent());
        assertEquals("CODE_001", found.get().getAchievementCode());
    }

    @Test
    void existsByUserIdAndAchievementCode_ReturnsTrueWhenExists() {
        assertTrue(repository.existsByUserIdAndAchievementCode(userId, "CODE_001"));
        assertFalse(repository.existsByUserIdAndAchievementCode(userId, "NON_EXISTENT"));
    }

    @Test
    void findByUserIdAndAchievementCode_ReturnsAchievement() {
        Optional<AchievementProgress> found = repository.findByUserIdAndAchievementCode(userId, "CODE_001");
        assertTrue(found.isPresent());
        assertEquals(achievement1.getId(), found.get().getId());
    }
}
