package com.campusguide.personal.ai.atlas.ratelimit;

public interface RateLimitPolicy {
    boolean tryAcquire(String userOrClientKey);
    void reset(String userOrClientKey);
    RateLimitStatus getStatus(String userOrClientKey);
}
