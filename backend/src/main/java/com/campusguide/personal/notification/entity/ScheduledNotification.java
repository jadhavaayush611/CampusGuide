package com.campusguide.personal.notification.entity;

import com.campusguide.personal.notification.enums.NotificationChannel;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationStatus;
import com.campusguide.personal.notification.enums.NotificationType;
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

@Document(collection = "scheduled_notifications")
@CompoundIndexes({
    @CompoundIndex(name = "user_status_scheduled_idx", def = "{'userId': 1, 'status': 1, 'scheduledFor': 1}"),
    @CompoundIndex(name = "status_scheduled_idx", def = "{'status': 1, 'scheduledFor': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledNotification {

    @Id
    private UUID id;

    @Indexed
    private String userId;

    private String title;

    private String message;

    private NotificationType type;

    @Indexed
    private NotificationStatus status;

    @Indexed
    private LocalDateTime scheduledFor;

    private LocalDateTime deliveredAt;

    private LocalDateTime readAt;

    @Indexed
    private UUID linkedPlannerTaskId;

    @Indexed
    private UUID linkedCalendarEntryId;

    @Indexed
    private UUID linkedEventId;

    @Indexed
    private UUID linkedAchievementId;

    private NotificationChannel channel;

    private NotificationPriority priority;

    private Map<String, Object> metadata;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public static class ScheduledNotificationBuilder {
        public ScheduledNotificationBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public ScheduledNotificationBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public ScheduledNotificationBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public ScheduledNotificationBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
