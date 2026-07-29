package com.campusguide.personal.ai.atlas.context.optimization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache policy configuration for context retrieval.
 */
@Component
@Data
@AllArgsConstructor
@Builder
public class RetrievalCachePolicy {

    private boolean enabled;
    private long defaultTtlMs;
    private int maxEntries;

    @Builder.Default
    private Map<String, Long> domainTtlMs = new HashMap<>();

    public RetrievalCachePolicy() {
        this.enabled = true;
        this.defaultTtlMs = 600_000L; // 10 minutes default
        this.maxEntries = 1000;
        this.domainTtlMs = new HashMap<>();
        this.domainTtlMs.put("campus", 300_000L); // 5 min
        this.domainTtlMs.put("userProfile", 1_800_000L); // 30 min
        this.domainTtlMs.put("academic", 600_000L); // 10 min
    }

    public long getTtlForDomain(String domain) {
        if (domain != null && domainTtlMs.containsKey(domain)) {
            return domainTtlMs.get(domain);
        }
        return defaultTtlMs;
    }
}
