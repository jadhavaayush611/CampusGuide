package com.campusguide.personal.ai.atlas.execution.runtime.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Result returned by a ToolAdapter / ToolExecutor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String invocationId;
    private String unitId;
    private ToolExecutionStatus status;

    @Builder.Default
    private Map<String, Object> outputData = new HashMap<>();

    private String errorMessage;
    private long executionDurationMs;

    @Builder.Default
    private Instant timestamp = Instant.now();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    public static ToolResult success(String invocationId, String unitId, Map<String, Object> data, long durationMs) {
        return ToolResult.builder()
                .invocationId(invocationId)
                .unitId(unitId)
                .status(ToolExecutionStatus.SUCCESS)
                .outputData(data != null ? data : new HashMap<>())
                .executionDurationMs(durationMs)
                .timestamp(Instant.now())
                .build();
    }

    public static ToolResult failure(String invocationId, String unitId, String error, long durationMs) {
        return ToolResult.builder()
                .invocationId(invocationId)
                .unitId(unitId)
                .status(ToolExecutionStatus.FAILURE)
                .errorMessage(error)
                .executionDurationMs(durationMs)
                .timestamp(Instant.now())
                .build();
    }

    public static ToolResult denied(String invocationId, String unitId, String reason) {
        return ToolResult.builder()
                .invocationId(invocationId)
                .unitId(unitId)
                .status(ToolExecutionStatus.DENIED)
                .errorMessage(reason)
                .executionDurationMs(0L)
                .timestamp(Instant.now())
                .build();
    }

    public static ToolResult waitingForApproval(String invocationId, String unitId, String message) {
        return ToolResult.builder()
                .invocationId(invocationId)
                .unitId(unitId)
                .status(ToolExecutionStatus.WAITING_FOR_APPROVAL)
                .errorMessage(message)
                .executionDurationMs(0L)
                .timestamp(Instant.now())
                .build();
    }
}
