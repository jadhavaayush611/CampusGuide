package com.campusguide.personal.ai.atlas.execution.validation;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.context.SecurityContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates permission context meets basic workflow execution criteria.
 */
@Component
public class PermissionsValidationRule implements ExecutionValidationRule {

    @Override
    public String getRuleName() {
        return "PermissionsValidationRule";
    }

    @Override
    public ValidationResult validate(ExecutionContext context, ExecutableWorkflow workflow) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("ExecutionContext is null for permission validation");
            return ValidationResult.invalid(violations);
        }

        SecurityContext securityContext = context.getSecurityContext();
        if (securityContext == null) {
            violations.add("SecurityContext is missing in ExecutionContext");
        }

        return ValidationResult.builder()
                .valid(violations.isEmpty())
                .violations(violations)
                .checkedRulesCount(1)
                .build();
    }
}
