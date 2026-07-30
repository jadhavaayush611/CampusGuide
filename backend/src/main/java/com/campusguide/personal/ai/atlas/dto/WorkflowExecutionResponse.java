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
public class WorkflowExecutionResponse {
    private String executionId;
    private String workflowId;
    private String status;
    private Map<String, Object> result;
    private Instant startedAt;
    private Instant completedAt;
    private Long executionTimeMs;
    private String errorMessage;
    private Map<String, Object> metadata;
}
