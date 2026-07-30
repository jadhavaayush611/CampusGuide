package com.campusguide.personal.ai.atlas.execution.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Production-ready execution contract defining input/output schemas, required capabilities, SLA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionContract implements Serializable {

    private static final long serialVersionUID = 1L;

    private String contractId;
    private String workflowId;

    @Builder.Default
    private List<String> requiredCapabilities = new ArrayList<>();

    @Builder.Default
    private Map<String, String> inputSchema = new HashMap<>();

    @Builder.Default
    private Map<String, String> outputSchema = new HashMap<>();

    @Builder.Default
    private long expectedDurationSeconds = 300L;

    @Builder.Default
    private long maxMemoryMb = 512L;

    @Builder.Default
    private String slaLevel = "STANDARD";

    public static ExecutionContract defaultContract(String workflowId) {
        return ExecutionContract.builder()
                .contractId("ctr_" + workflowId)
                .workflowId(workflowId)
                .expectedDurationSeconds(300L)
                .maxMemoryMb(512L)
                .slaLevel("STANDARD")
                .build();
    }
}
