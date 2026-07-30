package com.campusguide.personal.ai.atlas.execution.validation;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.tool.CapabilityRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that required capabilities are registered in CapabilityRegistry.
 */
@Component
public class CapabilityAvailabilityValidationRule implements ExecutionValidationRule {

    private final CapabilityRegistry capabilityRegistry;

    public CapabilityAvailabilityValidationRule(CapabilityRegistry capabilityRegistry) {
        this.capabilityRegistry = capabilityRegistry;
    }

    @Override
    public String getRuleName() {
        return "CapabilityAvailabilityValidationRule";
    }

    @Override
    public ValidationResult validate(ExecutionContext context, ExecutableWorkflow workflow) {
        List<String> warnings = new ArrayList<>();

        if (workflow != null && workflow.getStages() != null) {
            for (ExecutionStage stage : workflow.getStages()) {
                if (stage.getExecutionUnits() != null) {
                    for (ExecutionUnit unit : stage.getExecutionUnits()) {
                        String capId = unit.getTargetCapability();
                        if (capId != null && !capId.isBlank() && !capabilityRegistry.hasCapability(capId)) {
                            warnings.add("Capability " + capId + " for unit " + unit.getUnitId() + " is not registered in CapabilityRegistry");
                        }
                    }
                }
            }
        }

        return ValidationResult.builder()
                .valid(true) // Warnings do not fail validation unless strict policy enforced
                .warnings(warnings)
                .checkedRulesCount(1)
                .build();
    }
}
