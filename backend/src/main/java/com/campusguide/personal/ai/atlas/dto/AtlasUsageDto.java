package com.campusguide.personal.ai.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasUsageDto {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
}
