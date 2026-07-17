package com.campusguide.modules.notification.entity;

import com.campusguide.modules.notification.enums.NotificationPriority;
import com.campusguide.modules.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;

    private String message;

    private NotificationType type;

    private NotificationPriority priority;

    private boolean read;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}
