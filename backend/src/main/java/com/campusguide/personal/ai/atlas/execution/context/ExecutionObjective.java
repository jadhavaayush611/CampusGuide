package com.campusguide.personal.ai.atlas.execution.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Objective contract for execution preparation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionObjective implements Serializable {

    private static final long serialVersionUID = 1L;

    private String objectiveId;
    private String title;
    private String description;
    private String targetState;

    @Builder.Default
    private List<String> successCriteria = new ArrayList<>();

    @Builder.Default
    private int priority = 1;

    public static ExecutionObjective defaultObjective(String title) {
        return ExecutionObjective.builder()
                .objectiveId("exec_obj_default")
                .title(title != null ? title : "Default Execution Objective")
                .description("Default execution preparation objective")
                .targetState("SUCCESSFUL_WORKFLOW_PREPARATION")
                .priority(1)
                .build();
    }
}
