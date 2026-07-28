package com.campusguide.personal.notification.mapper;

import com.campusguide.personal.notification.dto.CreateScheduledNotificationRequest;
import com.campusguide.personal.notification.dto.ScheduledNotificationResponse;
import com.campusguide.personal.notification.entity.ScheduledNotification;
import com.campusguide.personal.notification.enums.NotificationStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Component
public class ScheduledNotificationMapper {

    public ScheduledNotification toEntity(CreateScheduledNotificationRequest request, String userId) {
        if (request == null) {
            return null;
        }
        return ScheduledNotification.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .status(NotificationStatus.SCHEDULED)
                .scheduledFor(request.getScheduledFor())
                .linkedPlannerTaskId(request.getLinkedPlannerTaskId())
                .linkedCalendarEntryId(request.getLinkedCalendarEntryId())
                .linkedEventId(request.getLinkedEventId())
                .linkedAchievementId(request.getLinkedAchievementId())
                .channel(request.getChannel())
                .priority(request.getPriority())
                .metadata(request.getMetadata())
                .build();
    }

    public ScheduledNotificationResponse toResponse(ScheduledNotification entity) {
        if (entity == null) {
            return null;
        }
        LocalDateTime createdAt = entity.getCreatedAt() != null
                ? LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneId.systemDefault())
                : null;
        LocalDateTime updatedAt = entity.getUpdatedAt() != null
                ? LocalDateTime.ofInstant(entity.getUpdatedAt(), ZoneId.systemDefault())
                : null;

        return ScheduledNotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType())
                .status(entity.getStatus())
                .scheduledFor(entity.getScheduledFor())
                .deliveredAt(entity.getDeliveredAt())
                .readAt(entity.getReadAt())
                .linkedPlannerTaskId(entity.getLinkedPlannerTaskId())
                .linkedCalendarEntryId(entity.getLinkedCalendarEntryId())
                .linkedEventId(entity.getLinkedEventId())
                .linkedAchievementId(entity.getLinkedAchievementId())
                .channel(entity.getChannel())
                .priority(entity.getPriority())
                .metadata(entity.getMetadata())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
