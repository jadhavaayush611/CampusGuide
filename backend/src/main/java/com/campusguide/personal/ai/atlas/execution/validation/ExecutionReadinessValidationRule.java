package com.campusguide.personal.ai.atlas.execution.validation;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.WorkflowStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates overall execution readiness status.
 */
@Component
public class ExecutionReadinessValidationRule implements ExecutionValidationRule {

    @Override
    public String getRuleName() {
        return "ExecutionReadinessValidationRule";
    }

    @Override
    public ValidationResult validate(ExecutionContext context, ExecutableWorkflow workflow) {
        List<String> violations = new ArrayList<>();

        if (workflow != null && workflow.getStatus() == WorkflowStatus.REJECTED) {
            violations.add("Workflow status is REJECTED");
        }

        return ValidationResult.builder()
                .valid(violations.isEmpty())
                .violations(violations)
                .checkedRulesCount(1)
                .build();
    }
}
