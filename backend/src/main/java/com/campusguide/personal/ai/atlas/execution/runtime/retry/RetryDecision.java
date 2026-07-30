package com.campusguide.personal.ai.atlas.execution.runtime.retry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Result of a retry evaluation indicating whether and how to retry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetryDecision implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean shouldRetry;
    private int attemptNumber;
    private long backoffMs;
    private RetryStrategy strategy;
    private String reason;

    public static RetryDecision retry(int attempt, long backoffMs, RetryStrategy strategy, String reason) {
        return RetryDecision.builder()
                .shouldRetry(true)
                .attemptNumber(attempt)
                .backoffMs(backoffMs)
                .strategy(strategy)
                .reason(reason)
                .build();
    }

    public static RetryDecision noRetry(int attempt, String reason) {
        return RetryDecision.builder()
                .shouldRetry(false)
                .attemptNumber(attempt)
                .backoffMs(0L)
                .strategy(RetryStrategy.NO_RETRY)
                .reason(reason)
                .build();
    }
}
