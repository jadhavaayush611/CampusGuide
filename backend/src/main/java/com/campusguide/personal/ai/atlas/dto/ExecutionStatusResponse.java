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
public class ExecutionStatusResponse {
    private String executionId;
    private String workflowId;
    private String userId;
    private String status;
    private Integer progressPercent;
    private String currentStep;
    private Instant startedAt;
    private Instant updatedAt;
    private Instant completedAt;
    private Map<String, Object> result;
    private String errorMessage;
}
