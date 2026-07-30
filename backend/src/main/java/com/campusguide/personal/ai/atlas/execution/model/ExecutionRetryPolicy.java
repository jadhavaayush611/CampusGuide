package com.campusguide.personal.ai.atlas.execution.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Retry policy configuration for execution units.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionRetryPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private int maxRetries = 3;

    @Builder.Default
    private long initialBackoffMs = 1000L;

    @Builder.Default
    private long maxBackoffMs = 10000L;

    @Builder.Default
    private double backoffMultiplier = 2.0;

    @Builder.Default
    private List<String> retryableExceptions = new ArrayList<>();

    public static ExecutionRetryPolicy defaultConfig() {
        return ExecutionRetryPolicy.builder()
                .maxRetries(3)
                .initialBackoffMs(1000L)
                .maxBackoffMs(10000L)
                .backoffMultiplier(2.0)
                .build();
    }
}
