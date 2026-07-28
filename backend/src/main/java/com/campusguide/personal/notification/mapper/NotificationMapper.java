package com.campusguide.personal.notification.mapper;

import com.campusguide.personal.notification.dto.request.CreateNotificationRequest;
import com.campusguide.personal.notification.dto.response.NotificationResponse;
import com.campusguide.personal.notification.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .priority(notification.getPriority())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(notification.getCreatedAt(), java.time.ZoneId.systemDefault()) : null)
                .build();
    }

    public Notification toEntity(CreateNotificationRequest request) {
        if (request == null) {
            return null;
        }
        return Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .priority(request.getPriority())
                .metadata(request.getMetadata() != null ? new HashMap<>(request.getMetadata()) : new HashMap<>())
                .read(false)
                .build();
    }
}
