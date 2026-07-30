package com.campusguide.personal.ai.atlas.execution.explanation;

import com.campusguide.personal.ai.atlas.execution.approval.ApprovalRequirement;
import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.risk.ExecutionRisk;
import com.campusguide.personal.ai.atlas.execution.rollback.RollbackPlan;
import com.campusguide.personal.ai.atlas.execution.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutionExplanationEngineTest {

    @Test
    @DisplayName("ExecutionExplanationEngine synthesizes structured explanation")
    void testSynthesizeExplanation() {
        ExecutionExplanationEngine engine = new ExecutionExplanationEngine();
        ExecutionContext ctx = ExecutionContext.fromExecutionPlan(null);
        ExecutableWorkflow wf = ExecutableWorkflow.fallback("wf_expl_test", "Explanation test");
        ValidationResult valRes = ValidationResult.valid();
        ExecutionRisk risk = ExecutionRisk.lowRisk();
        ApprovalRequirement appReq = ApprovalRequirement.none("wf_expl_test");
        RollbackPlan rbPlan = RollbackPlan.empty("wf_expl_test");

        ExecutionExplanation explanation = engine.explain(ctx, wf, valRes, risk, appReq, rbPlan);

        assertNotNull(explanation);
        assertNotNull(explanation.getExplanationId());
        assertNotNull(explanation.getSummary());
        assertNotNull(explanation.getReadinessRationale());
        assertFalse(explanation.getReasons().isEmpty());
        assertNotNull(explanation.getRiskSummary());
        assertNotNull(explanation.getApprovalSummary());
        assertNotNull(explanation.getRollbackSummary());
    }
}
