package com.campusguide.personal.ai.atlas.execution.engine;

import com.campusguide.personal.ai.atlas.execution.approval.ApprovalEngine;
import com.campusguide.personal.ai.atlas.execution.approval.ApprovalRequirement;
import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.explanation.ExecutionExplanation;
import com.campusguide.personal.ai.atlas.execution.explanation.ExecutionExplanationEngine;
import com.campusguide.personal.ai.atlas.execution.metrics.ExecutionPreparationMetrics;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.WorkflowStatus;
import com.campusguide.personal.ai.atlas.execution.resource.ResourceAllocation;
import com.campusguide.personal.ai.atlas.execution.resource.ResourceAnalyzer;
import com.campusguide.personal.ai.atlas.execution.risk.ExecutionRisk;
import com.campusguide.personal.ai.atlas.execution.risk.RiskAssessmentEngine;
import com.campusguide.personal.ai.atlas.execution.rollback.RollbackPlan;
import com.campusguide.personal.ai.atlas.execution.rollback.RollbackPlanner;
import com.campusguide.personal.ai.atlas.execution.tool.ToolResolver;
import com.campusguide.personal.ai.atlas.execution.validation.ExecutionValidator;
import com.campusguide.personal.ai.atlas.execution.validation.ValidationResult;
import com.campusguide.personal.ai.atlas.execution.workflow.ExecutableWorkflowBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Pipeline coordinating tool capability resolution, resource analysis, workflow assembly,
 * validation, risk assessment, approval determination, rollback planning, explainability,
 * and operational metrics collection for execution preparation.
 */
@Slf4j
@Component("executionPreparationPipeline")
public class ExecutionPreparationPipeline {

    private final ExecutableWorkflowBuilder workflowBuilder;
    private final ResourceAnalyzer resourceAnalyzer;
    private final ToolResolver toolResolver;
    private final ExecutionValidator validator;
    private final RiskAssessmentEngine riskEngine;
    private final ApprovalEngine approvalEngine;
    private final RollbackPlanner rollbackPlanner;
    private final ExecutionExplanationEngine explanationEngine;

    public ExecutionPreparationPipeline(ExecutableWorkflowBuilder workflowBuilder,
                                         ResourceAnalyzer resourceAnalyzer,
                                         ToolResolver toolResolver,
                                         ExecutionValidator validator,
                                         RiskAssessmentEngine riskEngine,
                                         ApprovalEngine approvalEngine,
                                         RollbackPlanner rollbackPlanner,
                                         ExecutionExplanationEngine explanationEngine) {
        this.workflowBuilder = workflowBuilder;
        this.resourceAnalyzer = resourceAnalyzer;
        this.toolResolver = toolResolver;
        this.validator = validator;
        this.riskEngine = riskEngine;
        this.approvalEngine = approvalEngine;
        this.rollbackPlanner = rollbackPlanner;
        this.explanationEngine = explanationEngine;
    }

    public ExecutableWorkflow prepare(ExecutionContext context) {
        long startTime = System.currentTimeMillis();
        String fallbackWorkflowId = "wf_" + UUID.randomUUID().toString().substring(0, 8);

        if (context == null) {
            log.warn("Null ExecutionContext provided to ExecutionPreparationPipeline");
            return ExecutableWorkflow.fallback(fallbackWorkflowId, "Null ExecutionContext provided");
        }

        try {
            // 1. Initial Executable Workflow Assembly
            ExecutableWorkflow workflow = workflowBuilder.buildWorkflow(context);

            // 2. Tool Capability Resolution
            ToolResolver.ToolResolutionResult toolResult = toolResolver.resolve(context, workflow.getStages());
            context.setAvailableCapabilities(toolResult.getResolvedCapabilities());

            // 3. Resource Analysis (Dry Run - No Allocation)
            ResourceAllocation allocation = resourceAnalyzer.analyzeResources(context, workflow);
            context.setResourceAvailability(allocation);
            workflow.setResourceRequirements(allocation.getRequirements());

            // 4. Execution Validation
            long valStart = System.currentTimeMillis();
            ValidationResult validationResult = validator.validate(context, workflow);
            long valLatency = System.currentTimeMillis() - valStart;

            // 5. Risk Assessment
            ExecutionRisk risk = riskEngine.assessRisk(context, workflow, allocation);
            workflow.setRiskAssessment(risk);

            // 6. Approval Evaluation
            ApprovalRequirement approvalReq = approvalEngine.evaluateApprovals(context, workflow, risk);
            workflow.setApprovalRequirement(approvalReq);

            // 7. Rollback Planning
            RollbackPlan rollbackPlan = rollbackPlanner.planRollback(context, workflow);
            workflow.setRollbackPlan(rollbackPlan);

            // 8. Explainability Synthesis
            ExecutionExplanation explanation = explanationEngine.explain(context, workflow, validationResult, risk, approvalReq, rollbackPlan);
            workflow.setExplanation(explanation);

            // Determine final workflow status based on validation and approvals
            if (!validationResult.isValid()) {
                workflow.setStatus(WorkflowStatus.REJECTED);
            } else if (approvalReq.isApprovalRequired()) {
                workflow.setStatus(WorkflowStatus.APPROVAL_REQUIRED);
            } else {
                workflow.setStatus(WorkflowStatus.READY);
            }

            // 9. Compute Operational Metrics (Never exposing sensitive payloads)
            long totalLatency = System.currentTimeMillis() - startTime;
            Map<String, Integer> unitsByType = new HashMap<>();
            int unitCount = 0;
            if (workflow.getStages() != null) {
                for (var stage : workflow.getStages()) {
                    if (stage.getExecutionUnits() != null) {
                        unitCount += stage.getExecutionUnits().size();
                        for (ExecutionUnit u : stage.getExecutionUnits()) {
                            unitsByType.merge(u.getUnitType().name(), 1, Integer::sum);
                        }
                    }
                }
            }

            ExecutionPreparationMetrics metrics = ExecutionPreparationMetrics.builder()
                    .preparationLatencyMs(totalLatency)
                    .validationLatencyMs(valLatency)
                    .totalStages(workflow.getStages() != null ? workflow.getStages().size() : 0)
                    .totalExecutionUnits(unitCount)
                    .totalCheckpoints(workflow.getCheckpoints() != null ? workflow.getCheckpoints().size() : 0)
                    .validationFailureCount(validationResult.getViolations() != null ? validationResult.getViolations().size() : 0)
                    .approvalRequired(approvalReq.isApprovalRequired())
                    .approvalLevel(approvalReq.getRequiredApprovalLevel().name())
                    .overallRiskScore(risk.getOverallRiskScore())
                    .riskCategory(risk.getRiskCategory().name())
                    .rollbackStepCount(rollbackPlan.getRollbackUnits().size())
                    .capabilityCoverageRatio(toolResult.isAllCapabilitiesResolved() ? 1.0 : 0.5)
                    .unitsByType(unitsByType)
                    .build();

            log.info("Execution preparation completed for workflowId={}, status={}, latencyMs={}",
                    workflow.getWorkflowId(), workflow.getStatus(), totalLatency);

            return workflow;

        } catch (Exception e) {
            log.error("Error executing ExecutionPreparationPipeline for contextId={}", context.getContextId(), e);
            return ExecutableWorkflow.fallback(fallbackWorkflowId, "Preparation error: " + e.getMessage());
        }
    }
}
