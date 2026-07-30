package com.campusguide.personal.ai.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtlasHealthResponse {
    private String status;
    private String subsystemReadiness;
    private Instant timestamp;
    private Map<String, SubsystemHealthDto> components;
}
