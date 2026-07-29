package com.campusguide.personal.ai.atlas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasNormalizedResponse {
    private String id;
    private String content;
    @Builder.Default
    private AtlasRole role = AtlasRole.ASSISTANT;
    private String providerName;
    private String modelUsed;
    private String finishReason;
    private AtlasUsageInfo usage;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
