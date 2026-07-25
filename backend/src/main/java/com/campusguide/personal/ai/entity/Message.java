package com.campusguide.personal.ai.entity;

import com.campusguide.personal.ai.enums.MessageRole;
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

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    private String id;

    @Indexed
    private String conversationId;

    private MessageRole role;

    private String content;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    private Instant timestamp;
}
