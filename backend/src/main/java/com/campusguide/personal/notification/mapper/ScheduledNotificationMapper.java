package com.campusguide.personal.notification.mapper;

import com.campusguide.personal.notification.dto.CreateScheduledNotificationRequest;
import com.campusguide.personal.notification.dto.ScheduledNotificationResponse;
import com.campusguide.personal.notification.entity.ScheduledNotification;
import com.campusguide.personal.notification.enums.NotificationStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ScheduledNotificationMapper {

    public ScheduledNotification toEntity(CreateScheduledNotificationRequest request, UUID userId) {
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
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
