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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@Document(collection = "achievement_progress")
@CompoundIndexes({
    @CompoundIndex(name = "user_achievement_code_idx", def = "{'userId': 1, 'achievementCode': 1}", unique = true),
    @CompoundIndex(name = "user_category_idx", def = "{'userId': 1, 'category': 1}"),
    @CompoundIndex(name = "user_status_idx", def = "{'userId': 1, 'status': 1}"),
    @CompoundIndex(name = "user_category_status_idx", def = "{'userId': 1, 'category': 1, 'status': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementProgress {

    @Id
    private UUID id;

    @Indexed
    private String userId;

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
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public static class AchievementProgressBuilder {
        public AchievementProgressBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public AchievementProgressBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public AchievementProgressBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public AchievementProgressBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
