package com.campusguide.personal.ai.atlas.execution.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Operational constraints governing execution preparation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionConstraints implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private long maxDurationSeconds = 3600L;

    @Builder.Default
    private int maxRetriesPerUnit = 3;

    @Builder.Default
    private Set<String> prohibitedCapabilities = new HashSet<>();

    @Builder.Default
    private Set<String> restrictedToolIds = new HashSet<>();

    @Builder.Default
    private boolean requireStrictRollback = true;

    @Builder.Default
    private int maxConcurrentUnits = 5;

    @Builder.Default
    private boolean requireExplicitApproval = false;

    public static ExecutionConstraints defaultConstraints() {
        return ExecutionConstraints.builder()
                .maxDurationSeconds(3600L)
                .maxRetriesPerUnit(3)
                .requireStrictRollback(true)
                .maxConcurrentUnits(5)
                .requireExplicitApproval(false)
                .build();
    }
}
