package com.campusguide.personal.achievement.dto;

import com.campusguide.personal.achievement.entity.AchievementCategory;
import com.campusguide.personal.achievement.entity.AchievementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementProgressResponse {

    private UUID id;
    private UUID userId;
    private String achievementCode;
    private String title;
    private String description;
    private AchievementCategory category;
    private AchievementStatus status;
    private Integer progress;
    private LocalDateTime earnedAt;
    private String evidenceUrl;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
