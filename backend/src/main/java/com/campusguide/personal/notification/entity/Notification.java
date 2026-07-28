package com.campusguide.personal.notification.entity;

import com.campusguide.personal.notification.enums.NotificationPriority;
import com.campusguide.personal.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

@Document(collection = "notifications")
@CompoundIndexes({
    @CompoundIndex(name = "user_read_idx", def = "{'userId': 1, 'read': 1}"),
    @CompoundIndex(name = "user_created_idx", def = "{'userId': 1, 'createdAt': -1}")
})
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

    @CreatedDate
    private Instant createdAt;

    private Instant readAt;

    public static class NotificationBuilder {
        public NotificationBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public NotificationBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
