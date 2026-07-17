package com.campusguide.modules.ai.entity;

import com.campusguide.modules.ai.enums.ConversationStatus;
import com.campusguide.modules.ai.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "conversations")
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

    private Instant createdAt;

    private Instant updatedAt;
}
