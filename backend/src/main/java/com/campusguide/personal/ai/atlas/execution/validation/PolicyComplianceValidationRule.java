package com.campusguide.personal.ai.atlas.execution.validation;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.context.ExecutionConstraints;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates execution policy and constraint compliance.
 */
@Component
public class PolicyComplianceValidationRule implements ExecutionValidationRule {

    @Override
    public String getRuleName() {
        return "PolicyComplianceValidationRule";
    }

    @Override
    public ValidationResult validate(ExecutionContext context, ExecutableWorkflow workflow) {
        List<String> violations = new ArrayList<>();

        if (context == null || workflow == null) {
            return ValidationResult.valid();
        }

        ExecutionConstraints constraints = context.getConstraints();
        if (constraints != null && constraints.getProhibitedCapabilities() != null) {
            for (ExecutionStage stage : workflow.getStages()) {
                if (stage.getExecutionUnits() != null) {
                    for (ExecutionUnit unit : stage.getExecutionUnits()) {
                        if (unit.getTargetCapability() != null &&
                                constraints.getProhibitedCapabilities().contains(unit.getTargetCapability())) {
                            violations.add("Execution unit " + unit.getUnitId() + " uses prohibited capability: " + unit.getTargetCapability());
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
