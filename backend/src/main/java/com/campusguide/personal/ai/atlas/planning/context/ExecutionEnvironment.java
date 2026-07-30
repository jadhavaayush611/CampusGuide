package com.campusguide.personal.ai.atlas.planning.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Execution environment descriptor for plan resolution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionEnvironment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String environmentName = "PRODUCTION";

    @Builder.Default
    private Set<String> supportedCapabilities = Collections.singleton("ALL");

    @Builder.Default
    private boolean offlineMode = false;

    public static ExecutionEnvironment defaultEnvironment() {
        return ExecutionEnvironment.builder()
                .environmentName("PRODUCTION")
                .supportedCapabilities(Collections.singleton("ALL"))
                .offlineMode(false)
                .build();
    }
}
