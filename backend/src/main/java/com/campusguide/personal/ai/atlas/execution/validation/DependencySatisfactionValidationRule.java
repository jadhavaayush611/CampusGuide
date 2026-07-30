package com.campusguide.personal.ai.atlas.execution.validation;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates unit dependency satisfaction within the workflow.
 */
@Component
public class DependencySatisfactionValidationRule implements ExecutionValidationRule {

    @Override
    public String getRuleName() {
        return "DependencySatisfactionValidationRule";
    }

    @Override
    public ValidationResult validate(ExecutionContext context, ExecutableWorkflow workflow) {
        List<String> violations = new ArrayList<>();

        if (workflow == null || workflow.getStages() == null) {
            return ValidationResult.valid();
        }

        Set<String> declaredUnitIds = new HashSet<>();
        Set<String> declaredTaskIds = new HashSet<>();

        for (ExecutionStage stage : workflow.getStages()) {
            if (stage.getExecutionUnits() != null) {
                for (ExecutionUnit unit : stage.getExecutionUnits()) {
                    declaredUnitIds.add(unit.getUnitId());
                    if (unit.getTaskId() != null) {
                        declaredTaskIds.add(unit.getTaskId());
                    }
                }
            }
        }

        for (ExecutionStage stage : workflow.getStages()) {
            if (stage.getExecutionUnits() != null) {
                for (ExecutionUnit unit : stage.getExecutionUnits()) {
                    if (unit.getDependencies() != null) {
                        for (String depId : unit.getDependencies()) {
                            if (!declaredUnitIds.contains(depId) && !declaredTaskIds.contains(depId)) {
                                violations.add("Execution unit " + unit.getUnitId() + " has unsatisfied dependency: " + depId);
                            }
                        }
                    }
                }
            }
        }

        return ValidationResult.builder()
                .valid(violations.isEmpty())
                .violations(violations)
                .checkedRulesCount(1)
                .build();
    }
}
