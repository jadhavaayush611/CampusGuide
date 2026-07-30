package com.campusguide.personal.ai.atlas.execution.validation;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * ExecutionValidator orchestrates the evaluation of validation rules against an ExecutionContext
 * and ExecutableWorkflow.
 */
@Slf4j
@Component
public class ExecutionValidator {

    private final List<ExecutionValidationRule> rules;

    public ExecutionValidator(List<ExecutionValidationRule> rules) {
        this.rules = rules != null ? rules : new ArrayList<>();
    }

    public ValidationResult validate(ExecutionContext context, ExecutableWorkflow workflow) {
        log.debug("Evaluating {} validation rules for workflowId={}",
                rules.size(), workflow != null ? workflow.getWorkflowId() : "unknown");

        List<String> violations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int checkedCount = 0;

        for (ExecutionValidationRule rule : rules) {
            try {
                ValidationResult result = rule.validate(context, workflow);
                checkedCount += result.getCheckedRulesCount();
                if (result.getViolations() != null) {
                    violations.addAll(result.getViolations());
                }
                if (result.getWarnings() != null) {
                    warnings.addAll(result.getWarnings());
                }
            } catch (Exception e) {
                log.error("Error executing validation rule {}", rule.getRuleName(), e);
                violations.add("Validation rule error in " + rule.getRuleName() + ": " + e.getMessage());
            }
        }

        boolean valid = violations.isEmpty();
        log.debug("Validation completed for workflowId={}, valid={}, violationsCount={}, warningsCount={}",
                workflow != null ? workflow.getWorkflowId() : "unknown", valid, violations.size(), warnings.size());

        return ValidationResult.builder()
                .valid(valid)
                .violations(violations)
                .warnings(warnings)
                .checkedRulesCount(checkedCount)
                .evaluatedAt(Instant.now())
                .build();
    }
}
