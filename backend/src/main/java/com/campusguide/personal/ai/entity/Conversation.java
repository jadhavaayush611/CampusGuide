package com.campusguide.personal.ai.entity;

import com.campusguide.personal.ai.enums.ConversationStatus;
import com.campusguide.personal.ai.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

@Document(collection = "conversations")
@CompoundIndexes({
    @CompoundIndex(name = "user_status_idx", def = "{'userId': 1, 'status': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;

    private ConversationType type;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Builder.Default
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public static class ConversationBuilder {
        public ConversationBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public ConversationBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public ConversationBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public ConversationBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
