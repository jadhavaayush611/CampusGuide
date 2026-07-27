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
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Document(collection = "scheduled_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledNotification {

    @Id
    private UUID id;

    @Indexed
    private UUID userId;

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
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
