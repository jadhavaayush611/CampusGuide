package com.campusguide.personal.ai.atlas.execution.workflow;

import com.campusguide.personal.ai.atlas.execution.model.ExecutionCheckpoint;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionRetryPolicy;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionRollbackPolicy;
import com.campusguide.personal.ai.atlas.execution.model.StageCompletionPolicy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Structural template for creating executable workflows.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    private String templateId;
    private String templateName;
    private String domain;

    @Builder.Default
    private StageCompletionPolicy defaultStageStrategy = StageCompletionPolicy.ALL_MUST_SUCCEED;

    @Builder.Default
    private ExecutionRetryPolicy defaultRetryPolicy = ExecutionRetryPolicy.defaultConfig();

    @Builder.Default
    private ExecutionRollbackPolicy defaultRollbackPolicy = ExecutionRollbackPolicy.defaultConfig();

    @Builder.Default
    private List<ExecutionCheckpoint> predefinedCheckpoints = new ArrayList<>();

    public static WorkflowTemplate defaultTemplate() {
        return WorkflowTemplate.builder()
                .templateId("tmpl_default")
                .templateName("Standard Executable Workflow Template")
                .domain("GENERAL")
                .defaultStageStrategy(StageCompletionPolicy.ALL_MUST_SUCCEED)
                .defaultRetryPolicy(ExecutionRetryPolicy.defaultConfig())
                .defaultRollbackPolicy(ExecutionRollbackPolicy.defaultConfig())
                .build();
    }
}
