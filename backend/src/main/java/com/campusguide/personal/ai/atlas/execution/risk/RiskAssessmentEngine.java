package com.campusguide.personal.ai.atlas.execution.risk;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnitType;
import com.campusguide.personal.ai.atlas.execution.resource.ResourceAllocation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * RiskAssessmentEngine evaluates execution complexity, dependency risk, security risk, resource risk,
 * and failure probability for an ExecutableWorkflow.
 */
@Slf4j
@Component
public class RiskAssessmentEngine {

    public ExecutionRisk assessRisk(ExecutionContext context, ExecutableWorkflow workflow, ResourceAllocation allocation) {
        log.debug("Evaluating execution risk for workflowId={}", workflow != null ? workflow.getWorkflowId() : "unknown");

        List<RiskFactor> factors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // 1. Execution Complexity Risk
        int unitCount = 0;
        int mutationCount = 0;
        if (workflow != null && workflow.getStages() != null) {
            for (ExecutionStage stage : workflow.getStages()) {
                if (stage.getExecutionUnits() != null) {
                    unitCount += stage.getExecutionUnits().size();
                    for (ExecutionUnit unit : stage.getExecutionUnits()) {
                        if (unit.getUnitType() == ExecutionUnitType.MUTATION) {
                            mutationCount++;
                        }
                    }
                }
            }
        }

        double complexityScore = Math.min(1.0, (unitCount * 0.05) + (mutationCount * 0.15));
        factors.add(RiskFactor.builder()
                .factorId("factor_complexity")
                .category(RiskFactorCategory.COMPLEXITY)
                .name("Workflow Unit Complexity")
                .score(complexityScore)
                .weight(0.25)
                .description("Evaluates workflow step count (" + unitCount + ") and mutation actions (" + mutationCount + ")")
                .build());

        // 2. Dependency Risk
        int stageCount = (workflow != null && workflow.getStages() != null) ? workflow.getStages().size() : 0;
        double dependencyScore = Math.min(1.0, stageCount * 0.1);
        factors.add(RiskFactor.builder()
                .factorId("factor_dependency")
                .category(RiskFactorCategory.DEPENDENCY)
                .name("Stage Dependency Depth")
                .score(dependencyScore)
                .weight(0.20)
                .description("Evaluates inter-stage pipeline depth (" + stageCount + " stages)")
                .build());

        // 3. Security Risk
        double securityScore = (mutationCount > 0) ? 0.4 : 0.1;
        if (context != null && context.getScope() != null && context.getScope().getImpactRadius() != null) {
            switch (context.getScope().getImpactRadius()) {
                case HIGH -> securityScore = Math.max(securityScore, 0.7);
                case CRITICAL -> securityScore = 0.9;
                default -> {}
            }
        }
        factors.add(RiskFactor.builder()
                .factorId("factor_security")
                .category(RiskFactorCategory.SECURITY)
                .name("Security & Isolation Risk")
                .score(securityScore)
                .weight(0.25)
                .description("Evaluates impact radius and system write operations")
                .build());

        // 4. Resource Risk
        double resourceScore = 0.1;
        if (allocation != null && !allocation.isAllRequirementsSatisfied()) {
            resourceScore = Math.min(1.0, 0.5 + (allocation.getMissingResourceCount() * 0.2));
            recommendations.add("Provide required missing resources (" + allocation.getMissingResourceCount() + " unsatisfied)");
        }
        factors.add(RiskFactor.builder()
                .factorId("factor_resource")
                .category(RiskFactorCategory.RESOURCE)
                .name("Resource Availability Risk")
                .score(resourceScore)
                .weight(0.15)
                .description("Evaluates tool and resource fulfillment status")
                .build());

        // 5. Failure Probability Risk
        double failureScore = Math.min(1.0, (complexityScore * 0.4) + (resourceScore * 0.4) + (securityScore * 0.2));
        factors.add(RiskFactor.builder()
                .factorId("factor_failure")
                .category(RiskFactorCategory.FAILURE_PROBABILITY)
                .name("Failure Probability Estimate")
                .score(failureScore)
                .weight(0.15)
                .description("Composite estimation of workflow execution failure probability")
                .build());

        // Calculate weighted composite score
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        for (RiskFactor factor : factors) {
            totalWeightedScore += (factor.getScore() * factor.getWeight());
            totalWeight += factor.getWeight();
        }
        double compositeScore = (totalWeight > 0) ? (totalWeightedScore / totalWeight) : 0.1;
        RiskScore riskScore = RiskScore.fromScore(compositeScore);

        if (riskScore.getLevel() == RiskScore.RiskLevel.HIGH || riskScore.getLevel() == RiskScore.RiskLevel.CRITICAL) {
            recommendations.add("Consider human approval and explicit stage verification checkpoints before execution");
        }

        return ExecutionRisk.builder()
                .assessmentId("risk_assess_" + UUID.randomUUID().toString().substring(0, 8))
                .overallRiskScore(compositeScore)
                .riskCategory(riskScore.getLevel())
                .factors(factors)
                .failureProbability(failureScore)
                .recommendations(recommendations)
                .build();
    }
}
