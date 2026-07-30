package com.campusguide.personal.ai.atlas.execution.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Audit metadata for execution-ready workflows.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private String workflowId;
    private String planId;
    private String contextId;
    private String version;
    private String generatorId;
    private String environment;

    @Builder.Default
    private Instant preparedAt = Instant.now();

    @Builder.Default
    private Instant validatedAt = Instant.now();

    private String checksum;

    @Builder.Default
    private Map<String, Object> customProperties = new HashMap<>();

    public static ExecutionMetadata createDefault(String workflowId, String planId, String contextId) {
        return ExecutionMetadata.builder()
                .workflowId(workflowId)
                .planId(planId)
                .contextId(contextId)
                .version("1.0.0")
                .generatorId("ExecutableWorkflowBuilder")
                .environment("PRODUCTION")
                .preparedAt(Instant.now())
                .validatedAt(Instant.now())
                .checksum("chk_" + System.currentTimeMillis())
                .build();
    }
}
