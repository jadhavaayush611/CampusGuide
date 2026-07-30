package com.campusguide.personal.ai.atlas.execution.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Execution boundary scope defining domain and isolation parameters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionScope implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String scopeId = "scope_default";

    @Builder.Default
    private List<String> targetDomains = new ArrayList<>();

    @Builder.Default
    private boolean readOnly = false;

    @Builder.Default
    private ImpactRadius impactRadius = ImpactRadius.LOW;

    @Builder.Default
    private boolean isolatedEnvironment = false;

    public enum ImpactRadius {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public static ExecutionScope defaultScope() {
        return ExecutionScope.builder()
                .scopeId("scope_default")
                .readOnly(false)
                .impactRadius(ImpactRadius.LOW)
                .isolatedEnvironment(false)
                .build();
    }
}
