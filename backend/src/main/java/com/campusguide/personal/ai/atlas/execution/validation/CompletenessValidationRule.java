package com.campusguide.personal.ai.atlas.execution.validation;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates workflow structural completeness.
 */
@Component
public class CompletenessValidationRule implements ExecutionValidationRule {

    @Override
    public String getRuleName() {
        return "CompletenessValidationRule";
    }

    @Override
    public ValidationResult validate(ExecutionContext context, ExecutableWorkflow workflow) {
        List<String> violations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (workflow == null) {
            violations.add("Workflow is null");
            return ValidationResult.invalid(violations);
        }

        if (workflow.getWorkflowId() == null || workflow.getWorkflowId().isBlank()) {
            violations.add("Workflow ID is missing");
        }

        if (workflow.getStages() == null || workflow.getStages().isEmpty()) {
            violations.add("Workflow contains no execution stages");
        } else {
            for (ExecutionStage stage : workflow.getStages()) {
                if (stage.getStageId() == null || stage.getStageId().isBlank()) {
                    violations.add("Stage ID is missing in workflow stage");
                }
                if (stage.getExecutionUnits() == null || stage.getExecutionUnits().isEmpty()) {
                    warnings.add("Stage " + stage.getStageId() + " contains no execution units");
                } else {
                    for (ExecutionUnit unit : stage.getExecutionUnits()) {
                        if (unit.getUnitId() == null || unit.getUnitId().isBlank()) {
                            violations.add("Unit ID is missing in stage " + stage.getStageId());
                        }
                    }
                }
            }
        }

        return ValidationResult.builder()
                .valid(violations.isEmpty())
                .violations(violations)
                .warnings(warnings)
                .checkedRulesCount(1)
                .build();
    }
}
