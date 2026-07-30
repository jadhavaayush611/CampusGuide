package com.campusguide.personal.ai.atlas.execution.risk;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.resource.ResourceAllocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RiskAssessmentEngineTest {

    @Test
    @DisplayName("RiskAssessmentEngine evaluates complexity, dependency, security, resource, and failure risk factors")
    void testAssessRisk() {
        RiskAssessmentEngine engine = new RiskAssessmentEngine();
        ExecutionContext ctx = ExecutionContext.fromExecutionPlan(null);
        ExecutableWorkflow wf = ExecutableWorkflow.fallback("wf_risk_test", "Risk assessment test");
        ResourceAllocation alloc = ResourceAllocation.empty();

        ExecutionRisk risk = engine.assessRisk(ctx, wf, alloc);

        assertNotNull(risk);
        assertNotNull(risk.getAssessmentId());
        assertTrue(risk.getOverallRiskScore() >= 0.0 && risk.getOverallRiskScore() <= 1.0);
        assertNotNull(risk.getRiskCategory());
        assertEquals(5, risk.getFactors().size());
    }
}
