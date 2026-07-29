package com.campusguide.personal.ai.atlas.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitStatus {
    private String key;
    private boolean allowed;
    private int remainingTokens;
    private int capacity;
    private long resetInSeconds;
}
