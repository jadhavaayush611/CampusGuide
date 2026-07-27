package com.campusguide.personal.achievement.mapper;

import com.campusguide.personal.achievement.dto.AchievementProgressResponse;
import com.campusguide.personal.achievement.dto.CreateAchievementRequest;
import com.campusguide.personal.achievement.entity.AchievementProgress;
import com.campusguide.personal.achievement.entity.AchievementStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AchievementProgressMapper {

    public AchievementProgress toEntity(CreateAchievementRequest request, UUID userId) {
        if (request == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        int progress = request.getProgress() != null ? request.getProgress() : 0;
        AchievementStatus status;
        LocalDateTime earnedAt = null;
        if (progress == 100) {
            status = AchievementStatus.EARNED;
            earnedAt = now;
        } else if (progress > 0) {
            status = AchievementStatus.IN_PROGRESS;
        } else {
            status = AchievementStatus.LOCKED;
        }

        return AchievementProgress.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .achievementCode(request.getAchievementCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(status)
                .progress(progress)
                .earnedAt(earnedAt)
                .evidenceUrl(request.getEvidenceUrl())
                .metadata(request.getMetadata())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public AchievementProgressResponse toResponse(AchievementProgress achievement) {
        if (achievement == null) {
            return null;
        }
        return AchievementProgressResponse.builder()
                .id(achievement.getId())
                .userId(achievement.getUserId())
                .achievementCode(achievement.getAchievementCode())
                .title(achievement.getTitle())
                .description(achievement.getDescription())
                .category(achievement.getCategory())
                .status(achievement.getStatus())
                .progress(achievement.getProgress())
                .earnedAt(achievement.getEarnedAt())
                .evidenceUrl(achievement.getEvidenceUrl())
                .metadata(achievement.getMetadata())
                .createdAt(achievement.getCreatedAt())
                .updatedAt(achievement.getUpdatedAt())
                .build();
    }
}
