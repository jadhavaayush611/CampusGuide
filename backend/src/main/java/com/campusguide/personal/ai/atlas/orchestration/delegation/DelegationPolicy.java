package com.campusguide.personal.ai.atlas.orchestration.delegation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Policy for governing task delegation decisions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DelegationPolicy {

    @Builder.Default
    private AssignmentStrategy strategy = AssignmentStrategy.HYBRID;
    @Builder.Default
    private double minCapabilityScore = 0.5;
    @Builder.Default
    private int maxLoadThreshold = 10;
    @Builder.Default
    private double localityWeight = 0.3;
    @Builder.Default
    private long timeoutMs = 30000;
    @Builder.Default
    private boolean fallbackAllowed = true;

    public static DelegationPolicy defaultPolicy() {
        return DelegationPolicy.builder().build();
    }
}
