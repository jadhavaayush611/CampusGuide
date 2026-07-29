package com.campusguide.personal.ai.atlas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasChatMessage {
    private AtlasRole role;
    private String content;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
