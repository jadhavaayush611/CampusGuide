package com.campusguide.personal.ai.atlas.execution.context;

import com.campusguide.personal.ai.atlas.context.model.UserContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.execution.approval.ApprovalPolicy;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionRetryPolicy;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionRollbackPolicy;
import com.campusguide.personal.ai.atlas.execution.resource.ResourceAllocation;
import com.campusguide.personal.ai.atlas.execution.tool.ToolCapability;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.PermissionContext;
import com.campusguide.personal.ai.atlas.planning.context.ExecutionEnvironment;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ExecutionContext serves as the contract between Planning and Execution Preparation.
 * Aggregates ExecutionPlan, PlanningContext, DecisionOutcome, UserContext, available tools,
 * available capabilities, permission context, security context, execution environment,
 * approval requirements, retry policies, rollback policies, monitoring configuration,
 * and resource availability.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionContext implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String contextId = "ectx_" + UUID.randomUUID().toString().substring(0, 8);

    private ExecutionPlan executionPlan;
    private PlanningContext planningContext;
    private DecisionOutcome decisionOutcome;
    private UserContext userContext;
    private String userId;

    @Builder.Default
    private Map<String, Object> availableTools = new HashMap<>();

    @Builder.Default
    private List<ToolCapability> availableCapabilities = new ArrayList<>();

    private PermissionContext permissionContext;

    @Builder.Default
    private SecurityContext securityContext = SecurityContext.defaultContext();

    @Builder.Default
    private ExecutionEnvironment executionEnvironment = ExecutionEnvironment.defaultEnvironment();

    @Builder.Default
    private List<ApprovalPolicy> approvalRequirements = Collections.singletonList(ApprovalPolicy.defaultPolicy());

    @Builder.Default
    private ExecutionRetryPolicy retryPolicies = ExecutionRetryPolicy.defaultConfig();

    @Builder.Default
    private ExecutionRollbackPolicy rollbackPolicies = ExecutionRollbackPolicy.defaultConfig();

    @Builder.Default
    private MonitoringConfiguration monitoringConfiguration = MonitoringConfiguration.defaultConfig();

    @Builder.Default
    private ResourceAllocation resourceAvailability = ResourceAllocation.empty();

    @Builder.Default
    private ExecutionObjective objective = ExecutionObjective.defaultObjective("default_execution_objective");

    @Builder.Default
    private ExecutionConstraints constraints = ExecutionConstraints.defaultConstraints();

    @Builder.Default
    private ExecutionScope scope = ExecutionScope.defaultScope();

    public static ExecutionContext fromExecutionPlan(ExecutionPlan plan) {
        return fromExecutionPlan(plan, null);
    }

    public static ExecutionContext fromExecutionPlan(ExecutionPlan plan, PlanningContext planningContext) {
        String planId = plan != null ? plan.getPlanId() : "plan_unknown";
        String goalTitle = (plan != null && plan.getGoal() != null) ? plan.getGoal().getTitle() : "Execution Plan Preparation";

        ExecutionContext ctx = ExecutionContext.builder()
                .contextId("ectx_" + planId)
                .executionPlan(plan)
                .planningContext(planningContext)
                .userId("system")
                .objective(ExecutionObjective.defaultObjective(goalTitle))
                .constraints(ExecutionConstraints.defaultConstraints())
                .scope(ExecutionScope.defaultScope())
                .securityContext(SecurityContext.defaultContext())
                .executionEnvironment(planningContext != null && planningContext.getExecutionEnvironment() != null ?
                        planningContext.getExecutionEnvironment() : ExecutionEnvironment.defaultEnvironment())
                .monitoringConfiguration(MonitoringConfiguration.defaultConfig())
                .retryPolicies(ExecutionRetryPolicy.defaultConfig())
                .rollbackPolicies(ExecutionRollbackPolicy.defaultConfig())
                .build();

        if (planningContext != null) {
            ctx.setDecisionOutcome(planningContext.getDecisionOutcome());
            ctx.setUserContext(planningContext.getUserContext());
            if (planningContext.getUserId() != null) {
                ctx.setUserId(planningContext.getUserId());
            }
            ctx.setPermissionContext(planningContext.getPermissionContext());
            if (planningContext.getAvailableResources() != null) {
                ctx.getAvailableTools().putAll(planningContext.getAvailableResources());
            }
        }

        return ctx;
    }
}
