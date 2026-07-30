package com.campusguide.personal.ai.atlas.execution.runtime.retry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed configuration policy for runtime retry operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetryPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private int maxRetries = 3;

    @Builder.Default
    private long initialBackoffMs = 1000L;

    @Builder.Default
    private long maxBackoffMs = 30000L;

    @Builder.Default
    private double backoffMultiplier = 2.0;

    @Builder.Default
    private RetryStrategy strategy = RetryStrategy.EXPONENTIAL_BACKOFF;

    @Builder.Default
    private List<String> retryableExceptions = new ArrayList<>();

    public static RetryPolicy defaultConfig() {
        return RetryPolicy.builder()
                .maxRetries(3)
                .initialBackoffMs(500L)
                .maxBackoffMs(10000L)
                .backoffMultiplier(2.0)
                .strategy(RetryStrategy.EXPONENTIAL_BACKOFF)
                .build();
    }

    public static RetryPolicy noRetry() {
        return RetryPolicy.builder()
                .maxRetries(0)
                .strategy(RetryStrategy.NO_RETRY)
                .build();
    }
}
