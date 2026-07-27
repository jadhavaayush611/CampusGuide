package com.campusguide.personal.achievement.validation;

import com.campusguide.personal.achievement.dto.CreateAchievementRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementProgressRequest;
import com.campusguide.personal.achievement.dto.UpdateAchievementRequest;
import com.campusguide.personal.achievement.entity.AchievementProgress;
import com.campusguide.personal.achievement.entity.AchievementStatus;
import com.campusguide.personal.achievement.exception.AchievementValidationException;
import org.springframework.stereotype.Component;

@Component
public class AchievementValidator {

    public void validateCreate(CreateAchievementRequest request) {
        if (request.getProgress() != null) {
            validateProgressRange(request.getProgress());
        }
    }

    public void validateUpdate(AchievementProgress existing, UpdateAchievementRequest request) {
        if (request.getProgress() != null) {
            validateProgressRange(request.getProgress());
            validateTransition(existing, request.getProgress());
        }
    }

    public void validateProgressUpdate(AchievementProgress existing, UpdateAchievementProgressRequest request) {
        if (request.getProgress() == null) {
            throw new AchievementValidationException("Progress must not be null");
        }
        validateProgressRange(request.getProgress());
        validateTransition(existing, request.getProgress());
    }

    public void validateProgressRange(int progress) {
        if (progress < 0 || progress > 100) {
            throw new AchievementValidationException("Progress must be within range [0, 100]");
        }
    }

    public void validateTransition(AchievementProgress existing, int newProgress) {
        if (existing.getStatus() == AchievementStatus.EARNED) {
            if (newProgress < 100) {
                throw new AchievementValidationException("Earned achievements cannot transition back to LOCKED");
            }
        }
    }
}
