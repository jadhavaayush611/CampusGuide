package com.campusguide.personal.ai.atlas.orchestration.replanning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

/**
 * Policy governing dynamic replanning conditions and constraints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplanningPolicy {

    @Builder.Default
    private int maxReplanAttempts = 3;
    @Builder.Default
    private boolean allowPartialReplan = true;
    @Builder.Default
    private Set<ReplanningTrigger> enabledTriggers = EnumSet.allOf(ReplanningTrigger.class);

    public static ReplanningPolicy defaultPolicy() {
        return ReplanningPolicy.builder().build();
    }
}
