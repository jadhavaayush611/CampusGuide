package com.campusguide.personal.ai.atlas.execution.validation;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;

/**
 * Interface for execution validation rules.
 */
public interface ExecutionValidationRule {

    String getRuleName();

    ValidationResult validate(ExecutionContext context, ExecutableWorkflow workflow);
}
