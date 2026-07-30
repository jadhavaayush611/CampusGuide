package com.campusguide.personal.ai.atlas.orchestration.memory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinator for managing leases, locks, and state access in SharedMemory while maintaining Execution Runtime as source of truth.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryCoordinator {

    private final SharedMemory sharedMemory;
    private final Map<String, MemoryLease> activeLeases = new ConcurrentHashMap<>();

    public Optional<MemoryLease> acquireLease(String namespace, String key, String agentId, long durationMs) {
        String leaseKey = buildLeaseKey(namespace, key);
        synchronized (this) {
            MemoryLease existing = activeLeases.get(leaseKey);
            if (existing != null && !existing.isExpired()) {
                if (existing.getHolderAgentId().equals(agentId)) {
                    log.debug("Renewing existing lease for agent {} on {}:{}", agentId, namespace, key);
                    return Optional.of(existing);
                }
                log.warn("Lease acquisition rejected for agent {} on {}:{}. Currently held by {}",
                        agentId, namespace, key, existing.getHolderAgentId());
                return Optional.empty();
            }

            MemoryLease newLease = MemoryLease.create(namespace, key, agentId, durationMs);
            activeLeases.put(leaseKey, newLease);
            
            Optional<SharedState> stateOpt = sharedMemory.get(namespace, key);
            stateOpt.ifPresent(state -> state.setActiveLease(newLease));

            log.info("Granted memory lease {} to agent {} on {}:{}", newLease.getLeaseId(), agentId, namespace, key);
            return Optional.of(newLease);
        }
    }

    public boolean releaseLease(MemoryLease lease) {
        if (lease == null) return false;
        String leaseKey = buildLeaseKey(lease.getNamespace(), lease.getKey());
        synchronized (this) {
            MemoryLease existing = activeLeases.get(leaseKey);
            if (existing != null && existing.getLeaseId().equals(lease.getLeaseId())) {
                existing.release();
                activeLeases.remove(leaseKey);

                Optional<SharedState> stateOpt = sharedMemory.get(lease.getNamespace(), lease.getKey());
                stateOpt.ifPresent(state -> state.setActiveLease(null));

                log.info("Released memory lease {} for agent {}", lease.getLeaseId(), lease.getHolderAgentId());
                return true;
            }
        }
        return false;
    }

    public boolean updateStateWithLease(MemoryLease lease, Object value) {
        if (lease == null || lease.isExpired()) {
            log.warn("Cannot update state with invalid or expired lease");
            return false;
        }

        String leaseKey = buildLeaseKey(lease.getNamespace(), lease.getKey());
        MemoryLease current = activeLeases.get(leaseKey);
        if (current == null || !current.getLeaseId().equals(lease.getLeaseId())) {
            log.warn("Lease mismatch during state update for key {}", leaseKey);
            return false;
        }

        sharedMemory.put(lease.getNamespace(), lease.getKey(), value, lease.getHolderAgentId());
        return true;
    }

    private String buildLeaseKey(String namespace, String key) {
        return namespace + "::" + key;
    }
}
