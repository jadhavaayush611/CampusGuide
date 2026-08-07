package com.campusguide.personal.ai.entity;

import com.campusguide.personal.ai.enums.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

@Document(collection = "messages")
@CompoundIndexes({
    @CompoundIndex(name = "conv_timestamp_idx", def = "{'conversationId': 1, 'timestamp': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    private String id;

    @jakarta.validation.constraints.NotBlank(message = "Conversation ID must not be blank")
    @Indexed
    private String conversationId;

    private MessageRole role;

    @jakarta.validation.constraints.NotBlank(message = "Content must not be blank")
    private String content;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @CreatedDate
    private Instant timestamp;

    @org.springframework.data.annotation.Version
    private Long version;
}
