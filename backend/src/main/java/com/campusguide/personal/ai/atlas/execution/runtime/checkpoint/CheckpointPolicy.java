package com.campusguide.personal.ai.atlas.execution.runtime.checkpoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Policy governing automatic and manual checkpoint creation during execution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckpointPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private boolean enablePreStageCheckpoints = true;

    @Builder.Default
    private boolean enablePostStageCheckpoints = true;

    @Builder.Default
    private boolean checkpointOnFailure = true;

    @Builder.Default
    private int maxCheckpointsPerWorkflow = 50;

    public static CheckpointPolicy defaultConfig() {
        return CheckpointPolicy.builder()
                .enablePreStageCheckpoints(true)
                .enablePostStageCheckpoints(true)
                .checkpointOnFailure(true)
                .maxCheckpointsPerWorkflow(50)
                .build();
    }
}
