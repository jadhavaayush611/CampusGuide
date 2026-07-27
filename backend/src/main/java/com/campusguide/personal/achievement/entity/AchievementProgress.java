package com.campusguide.personal.achievement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Document(collection = "achievement_progress")
@CompoundIndexes({
    @CompoundIndex(name = "user_achievement_code_idx", def = "{'userId': 1, 'achievementCode': 1}", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementProgress {

    @Id
    private UUID id;

    @Indexed
    private UUID userId;

    @Indexed
    private String achievementCode;

    private String title;

    private String description;

    private AchievementCategory category;

    private AchievementStatus status;

    private Integer progress;

    private LocalDateTime earnedAt;

    private String evidenceUrl;

    private Map<String, Object> metadata;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
