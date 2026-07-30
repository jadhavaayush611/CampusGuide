package com.campusguide.personal.ai.atlas.execution.model;

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
 * Stage of execution containing ordered/parallel execution units.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionStage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String stageId;
    private String stageName;
    private int orderIndex;

    @Builder.Default
    private List<ExecutionUnit> executionUnits = new ArrayList<>();

    @Builder.Default
    private boolean parallel = false;

    @Builder.Default
    private StageCompletionPolicy completionPolicy = StageCompletionPolicy.ALL_MUST_SUCCEED;

    private String stagePrecondition;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
