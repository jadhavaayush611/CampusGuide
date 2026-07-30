package com.campusguide.personal.ai.atlas.orchestration.coordination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Policy for governing agent synchronization, dependency enforcement, and conflict resolution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordinationPolicy {

    @Builder.Default
    private long timeoutMs = 60000;
    @Builder.Default
    private MergeStrategy mergeStrategy = MergeStrategy.UNION;
    @Builder.Default
    private ConflictResolutionPolicy conflictPolicy = ConflictResolutionPolicy.LATEST_WINS;
    @Builder.Default
    private boolean strictBarrier = true;

    public enum MergeStrategy {
        UNION,
        OVERWRITE,
        CONCATENATE,
        CUSTOM
    }

    public enum ConflictResolutionPolicy {
        LATEST_WINS,
        PRIORITY_WINS,
        FAIL_ON_CONFLICT
    }

    public static CoordinationPolicy defaultPolicy() {
        return CoordinationPolicy.builder().build();
    }
}
