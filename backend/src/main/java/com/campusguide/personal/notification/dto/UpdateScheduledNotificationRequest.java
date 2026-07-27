package com.campusguide.personal.notification.dto;

import com.campusguide.personal.notification.enums.NotificationChannel;
import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class UpdateScheduledNotificationRequest {

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Message is mandatory")
    private String message;

    @NotNull(message = "Notification type is mandatory")
    private NotificationType type;

    @NotNull(message = "Scheduled date/time is mandatory")
    private LocalDateTime scheduledFor;

    private UUID linkedPlannerTaskId;
    private UUID linkedCalendarEntryId;
    private UUID linkedEventId;
    private UUID linkedAchievementId;

    @NotNull(message = "Notification channel is mandatory")
    private NotificationChannel channel;

    @NotNull(message = "Notification priority is mandatory")
    private NotificationPriority priority;

    private Map<String, Object> metadata;
}
