package com.campusguide.personal.notification.dto;

import com.campusguide.personal.notification.enums.NotificationChannel;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationStatus;
import com.campusguide.personal.notification.enums.NotificationType;
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
public class ScheduledNotificationResponse {

    private UUID id;
    private String userId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private LocalDateTime scheduledFor;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private UUID linkedPlannerTaskId;
    private UUID linkedCalendarEntryId;
    private UUID linkedEventId;
    private UUID linkedAchievementId;
    private NotificationChannel channel;
    private NotificationPriority priority;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
