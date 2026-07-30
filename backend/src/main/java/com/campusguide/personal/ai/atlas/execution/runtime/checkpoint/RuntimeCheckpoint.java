package com.campusguide.personal.ai.atlas.execution.runtime.checkpoint;

import com.campusguide.personal.ai.atlas.execution.model.CheckpointType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Runtime checkpoint capturing execution progress and state snapshots.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuntimeCheckpoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String checkpointId = "chk_" + UUID.randomUUID().toString().substring(0, 8);

    private String workflowId;
    private String stageId;

    @Builder.Default
    private CheckpointType checkpointType = CheckpointType.PRE_STAGE;

    private StateSnapshot snapshot;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private String description;
}
