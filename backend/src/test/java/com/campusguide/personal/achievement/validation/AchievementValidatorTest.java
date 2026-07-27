package com.campusguide.personal.achievement.validation;

import com.campusguide.personal.achievement.dto.CreateAchievementRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementProgressRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementRequest;
import com.campusguide.personal.achievement.entity.AchievementCategory;
import com.campusguide.personal.achievement.entity.AchievementProgress;
import com.campusguide.personal.achievement.entity.AchievementStatus;
import com.campusguide.personal.achievement.exception.AchievementValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AchievementValidatorTest {

    private AchievementValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AchievementValidator();
    }

    @Test
    void validateCreate_ValidProgress_DoesNotThrow() {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .achievementCode("CODE_1")
                .title("Title")
                .category(AchievementCategory.ACADEMIC)
                .progress(50)
                .build();

        assertDoesNotThrow(() -> validator.validateCreate(request));
    }

    @Test
    void validateProgressRange_InvalidProgress_ThrowsException() {
        assertThrows(AchievementValidationException.class, () -> validator.validateProgressRange(-5));
        assertThrows(AchievementValidationException.class, () -> validator.validateProgressRange(105));
    }

    @Test
    void validateTransition_EarnedToLessThan100_ThrowsException() {
        AchievementProgress existing = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .status(AchievementStatus.EARNED)
                .progress(100)
                .build();

        assertThrows(AchievementValidationException.class, () -> validator.validateTransition(existing, 80));
    }

    @Test
    void validateTransition_EarnedTo100_DoesNotThrow() {
        AchievementProgress existing = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .status(AchievementStatus.EARNED)
                .progress(100)
                .build();

        assertDoesNotThrow(() -> validator.validateTransition(existing, 100));
    }

    @Test
    void validateProgressUpdate_NullProgress_ThrowsException() {
        AchievementProgress existing = AchievementProgress.builder()
                .id(UUID.randomUUID())
                .status(AchievementStatus.LOCKED)
                .progress(0)
                .build();

        UpdateAchievementProgressRequest request = UpdateAchievementProgressRequest.builder()
                .progress(null)
                .build();

        assertThrows(AchievementValidationException.class, () -> validator.validateProgressUpdate(existing, request));
    }
}
