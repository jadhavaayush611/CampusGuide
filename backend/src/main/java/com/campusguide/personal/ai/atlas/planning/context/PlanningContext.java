package com.campusguide.personal.ai.atlas.planning.context;

import com.campusguide.personal.ai.atlas.context.model.UserContext;
import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.context.DecisionObjective;
import com.campusguide.personal.ai.atlas.decision.context.EnvironmentalSignals;
import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.PermissionContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aggregated contract between the Decision Engine and the Planning Engine.
 * Encapsulates DecisionOutcome, DecisionObjective, DecisionContext, user context,
 * available resources, scheduling preferences, environmental signals, time horizon,
 * permission context, execution environment, and planning preferences.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningContext implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String contextId = "pctx_" + UUID.randomUUID().toString().substring(0, 8);

    private DecisionOutcome decisionOutcome;
    private DecisionObjective decisionObjective;
    private DecisionContext decisionContext;
    private UserContext userContext;
    private String userId;

    @Builder.Default
    private Map<String, Object> availableResources = new ConcurrentHashMap<>();

    @Builder.Default
    private SchedulingPreferences schedulingPreferences = SchedulingPreferences.defaultPreferences();

    @Builder.Default
    private EnvironmentalSignals environmentalSignals = EnvironmentalSignals.defaultSignals();

    @Builder.Default
    private TimeHorizon timeHorizon = TimeHorizon.defaultHorizon();

    private PermissionContext permissionContext;

    @Builder.Default
    private ExecutionEnvironment executionEnvironment = ExecutionEnvironment.defaultEnvironment();

    @Builder.Default
    private PlanningPreferences planningPreferences = PlanningPreferences.defaultPreferences();

    @Builder.Default
    private PlanningObjective objective = PlanningObjective.defaultObjective("default_planning_objective");

    @Builder.Default
    private PlanningConstraints constraints = PlanningConstraints.defaultConstraints();

    @Builder.Default
    private PlanningScope scope = PlanningScope.defaultScope();

    public static PlanningContext fromDecisionOutcome(DecisionOutcome outcome) {
        String outcomeId = outcome != null ? outcome.getOutcomeId() : "out_unknown";
        String rationale = outcome != null && outcome.getDecision() != null ? outcome.getDecision().getRationale() : "Default decision outcome";

        PlanningContext ctx = PlanningContext.builder()
                .contextId("pctx_" + outcomeId)
                .decisionOutcome(outcome)
                .userId("system")
                .objective(PlanningObjective.defaultObjective(rationale))
                .constraints(PlanningConstraints.defaultConstraints())
                .scope(PlanningScope.defaultScope())
                .schedulingPreferences(SchedulingPreferences.defaultPreferences())
                .environmentalSignals(EnvironmentalSignals.defaultSignals())
                .timeHorizon(TimeHorizon.defaultHorizon())
                .executionEnvironment(ExecutionEnvironment.defaultEnvironment())
                .planningPreferences(PlanningPreferences.defaultPreferences())
                .build();

        if (outcome != null && outcome.getExecutionHints() != null) {
            ctx.getAvailableResources().putAll(outcome.getExecutionHints());
        }

        return ctx;
    }

    public static PlanningContext fromDecisionContext(DecisionContext decCtx, DecisionOutcome outcome) {
        PlanningContext ctx = fromDecisionOutcome(outcome);
        if (decCtx != null) {
            ctx.setDecisionContext(decCtx);
            ctx.setDecisionObjective(decCtx.getObjective());
            ctx.setUserId(decCtx.getUserId());
            ctx.setUserContext(decCtx.getUserContext());
            ctx.setEnvironmentalSignals(decCtx.getEnvironmentalSignals());
            if (decCtx.getGraphContext() != null) {
                ctx.setPermissionContext(decCtx.getGraphContext().getPermissionContext());
            }
        }
        return ctx;
    }
}
