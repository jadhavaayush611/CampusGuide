package com.campusguide.personal.ai.atlas.streaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasStreamEvent {
    private String id;
    private StreamEventType type;
    private String conversationId;
    private String executionId;
    private Object payload;
    @Builder.Default
    private Instant timestamp = Instant.now();
    private long sequence;
}
