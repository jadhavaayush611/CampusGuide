package com.campusguide.personal.ai.atlas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasUsageInfo {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
}
