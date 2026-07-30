package com.campusguide.personal.ai.atlas.execution.approval;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnitType;
import com.campusguide.personal.ai.atlas.execution.risk.ExecutionRisk;
import com.campusguide.personal.ai.atlas.execution.risk.RiskScore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApprovalEngineTest {

    @Test
    @DisplayName("ApprovalEngine requires no approval for low risk read-only workflow")
    void testNoApprovalRequired() {
        ApprovalEngine engine = new ApprovalEngine();
        ExecutionContext ctx = ExecutionContext.fromExecutionPlan(null);
        ExecutableWorkflow wf = ExecutableWorkflow.fallback("wf_app_1", "Test read workflow");
        ExecutionRisk risk = ExecutionRisk.lowRisk();

        ApprovalRequirement req = engine.evaluateApprovals(ctx, wf, risk);

        assertNotNull(req);
        assertFalse(req.isApprovalRequired());
        assertEquals(ApprovalRequirement.ApprovalLevel.NONE, req.getRequiredApprovalLevel());
        assertTrue(req.isAutoApprovable());
    }

    @Test
    @DisplayName("ApprovalEngine enforces approval for high risk or mutation workflows")
    void testHighRiskApprovalRequired() {
        ApprovalEngine engine = new ApprovalEngine();
        ExecutionContext ctx = ExecutionContext.fromExecutionPlan(null);

        ExecutionUnit mutUnit = ExecutionUnit.builder()
                .unitId("u_mut")
                .unitType(ExecutionUnitType.MUTATION)
                .title("Update Database Record")
                .build();

        ExecutionStage stage = ExecutionStage.builder()
                .stageId("s_mut")
                .executionUnits(List.of(mutUnit))
                .build();

        ExecutableWorkflow wf = ExecutableWorkflow.builder()
                .workflowId("wf_mut")
                .stages(List.of(stage))
                .build();

        ExecutionRisk risk = ExecutionRisk.builder()
                .overallRiskScore(0.8)
                .riskCategory(RiskScore.RiskLevel.HIGH)
                .build();

        ApprovalRequirement req = engine.evaluateApprovals(ctx, wf, risk);

        assertNotNull(req);
        assertTrue(req.isApprovalRequired());
        assertEquals(ApprovalRequirement.ApprovalLevel.HIGH, req.getRequiredApprovalLevel());
        assertFalse(req.getApprovalReasons().isEmpty());
    }
}
