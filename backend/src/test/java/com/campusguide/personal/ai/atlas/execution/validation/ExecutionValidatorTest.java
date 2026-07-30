package com.campusguide.personal.ai.atlas.execution.validation;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.tool.CapabilityRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutionValidatorTest {

    private ExecutionValidator validator;

    @BeforeEach
    void setUp() {
        CapabilityRegistry registry = new CapabilityRegistry();
        List<ExecutionValidationRule> rules = List.of(
                new CompletenessValidationRule(),
                new DependencySatisfactionValidationRule(),
                new PolicyComplianceValidationRule(),
                new PermissionsValidationRule(),
                new CapabilityAvailabilityValidationRule(registry),
                new ExecutionReadinessValidationRule()
        );
        validator = new ExecutionValidator(rules);
    }

    @Test
    @DisplayName("ExecutionValidator validates valid ExecutableWorkflow")
    void testValidWorkflowValidation() {
        ExecutableWorkflow wf = ExecutableWorkflow.fallback("wf_val_test", "Validation test");
        ExecutionContext ctx = ExecutionContext.fromExecutionPlan(null);

        ValidationResult result = validator.validate(ctx, wf);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertTrue(result.getViolations().isEmpty());
        assertEquals(6, result.getCheckedRulesCount());
    }

    @Test
    @DisplayName("ExecutionValidator detects null workflow violation")
    void testNullWorkflowValidation() {
        ExecutionContext ctx = ExecutionContext.fromExecutionPlan(null);
        ValidationResult result = validator.validate(ctx, null);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertFalse(result.getViolations().isEmpty());
    }
}
