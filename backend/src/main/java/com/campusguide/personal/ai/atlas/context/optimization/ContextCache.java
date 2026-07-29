package com.campusguide.personal.ai.atlas.context.optimization;

import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe cache storing retrieved evidence bundles with configurable TTL, invalidation capabilities,
 * hit/miss metrics, and diagnostic reporting.
 */
@Component
@Slf4j
public class ContextCache {

    @Getter
    private final RetrievalCachePolicy policy;

    private final Map<String, CacheEntry> cacheStore = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);

    @Autowired
    public ContextCache(@Autowired(required = false) RetrievalCachePolicy policy) {
        this.policy = policy != null ? policy : new RetrievalCachePolicy();
    }

    public ContextCache() {
        this.policy = new RetrievalCachePolicy();
    }

    public void put(String key, EvidenceBundle bundle) {
        if (!policy.isEnabled() || key == null || bundle == null) return;

        if (cacheStore.size() >= policy.getMaxEntries()) {
            evictOldest();
        }

        String domain = bundle.getTargetDomain() != null ? bundle.getTargetDomain() : "default";
        long ttl = policy.getTtlForDomain(domain);
        long expiryTime = System.currentTimeMillis() + ttl;

        cacheStore.put(key, new CacheEntry(bundle, expiryTime, domain));
    }

    public Optional<EvidenceBundle> get(String key) {
        if (!policy.isEnabled() || key == null) {
            missCount.incrementAndGet();
            return Optional.empty();
        }

        CacheEntry entry = cacheStore.get(key);
        if (entry == null) {
            missCount.incrementAndGet();
            return Optional.empty();
        }

        if (System.currentTimeMillis() > entry.expiryTime()) {
            cacheStore.remove(key);
            evictionCount.incrementAndGet();
            missCount.incrementAndGet();
            return Optional.empty();
        }

        hitCount.incrementAndGet();
        return Optional.of(entry.bundle());
    }

    public void invalidateKey(String key) {
        if (key != null) {
            cacheStore.remove(key);
        }
    }

    public void invalidateDomain(String domain) {
        if (domain == null) return;
        cacheStore.entrySet().removeIf(e -> domain.equalsIgnoreCase(e.getValue().domain()));
    }

    public void clear() {
        cacheStore.clear();
    }

    public double getHitRatio() {
        long hits = hitCount.get();
        long total = hits + missCount.get();
        return total == 0 ? 0.0 : (double) hits / total;
    }

    public CacheDiagnostics getDiagnostics() {
        return new CacheDiagnostics(
                hitCount.get(),
                missCount.get(),
                getHitRatio(),
                evictionCount.get(),
                cacheStore.size(),
                policy.isEnabled()
        );
    }

    private synchronized void evictOldest() {
        if (cacheStore.isEmpty()) return;
        String oldestKey = null;
        long earliestExpiry = Long.MAX_VALUE;

        for (Map.Entry<String, CacheEntry> entry : cacheStore.entrySet()) {
            if (entry.getValue().expiryTime() < earliestExpiry) {
                earliestExpiry = entry.getValue().expiryTime();
                oldestKey = entry.getKey();
            }
        }

        if (oldestKey != null) {
            cacheStore.remove(oldestKey);
            evictionCount.incrementAndGet();
        }
    }

    public record CacheEntry(EvidenceBundle bundle, long expiryTime, String domain) {}

    public record CacheDiagnostics(
            long hits,
            long misses,
            double hitRatio,
            long evictions,
            int currentSize,
            boolean cacheEnabled
    ) {}
}
