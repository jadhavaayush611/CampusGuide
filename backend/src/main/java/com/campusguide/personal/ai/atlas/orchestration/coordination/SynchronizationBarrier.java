package com.campusguide.personal.ai.atlas.orchestration.coordination;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Barrier mechanism for synchronizing N parallel agent executions at a checkpoint.
 */
@Slf4j
public class SynchronizationBarrier {

    @Getter
    private final String barrierId;
    @Getter
    private final int expectedCount;
    private final CountDownLatch latch;
    private final Map<String, Object> arrivedResults = new ConcurrentHashMap<>();

    public SynchronizationBarrier(int expectedCount) {
        this.barrierId = "barrier_" + UUID.randomUUID().toString().substring(0, 8);
        this.expectedCount = Math.max(1, expectedCount);
        this.latch = new CountDownLatch(this.expectedCount);
    }

    public boolean arrive(String agentId, Object resultData) {
        if (agentId == null) return false;
        if (resultData != null) {
            arrivedResults.put(agentId, resultData);
        }
        latch.countDown();
        log.info("Agent {} arrived at barrier {} (Remaining: {})", agentId, barrierId, latch.getCount());
        return true;
    }

    public boolean await(long timeoutMs) throws InterruptedException {
        return latch.await(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public boolean isReleased() {
        return latch.getCount() == 0;
    }

    public Map<String, Object> getArrivedResults() {
        return new ConcurrentHashMap<>(arrivedResults);
    }
}
