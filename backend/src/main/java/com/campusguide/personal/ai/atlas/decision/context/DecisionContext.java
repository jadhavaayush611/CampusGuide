package com.campusguide.personal.ai.atlas.decision.context;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.model.UserContext;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-level aggregate context for the Decision Engine.
 * Combines GraphContext, ReasoningEvidence, user context, permissions, active collections,
 * confidence thresholds, environmental signals, request metadata, objective, constraints, and scope.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionContext implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String contextId = "dctx_" + UUID.randomUUID().toString().substring(0, 8);

    private GraphContext graphContext;
    private ReasoningEvidence reasoningEvidence;
    private AtlasContext atlasContext;
    private UserContext userContext;

    private String userId;

    @Builder.Default
    private Set<String> roles = Collections.emptySet();

    @Builder.Default
    private Set<String> permissions = Collections.emptySet();

    @Builder.Default
    private Set<String> activeCollections = Collections.emptySet();

    @Builder.Default
    private double confidenceThreshold = 0.50;

    @Builder.Default
    private EnvironmentalSignals environmentalSignals = EnvironmentalSignals.defaultSignals();

    @Builder.Default
    private RequestMetadata requestMetadata = RequestMetadata.create(null, "WEB_APP");

    @Builder.Default
    private DecisionObjective objective = DecisionObjective.defaultObjective("general_decision");

    @Builder.Default
    private DecisionConstraints constraints = DecisionConstraints.defaultConstraints();

    @Builder.Default
    private DecisionScope scope = DecisionScope.defaultScope();

    @Builder.Default
    private Map<String, Object> extensionAttributes = new ConcurrentHashMap<>();

    public void setExtension(String key, Object extension) {
        if (key != null && extension != null) {
            extensionAttributes.put(key, extension);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getExtension(String key, Class<T> clazz) {
        Object val = extensionAttributes.get(key);
        if (clazz.isInstance(val)) {
            return Optional.of((T) val);
        }
        return Optional.empty();
    }

    public static DecisionContext fromReasoning(GraphContext graphContext, ReasoningEvidence evidence) {
        String userId = graphContext != null && graphContext.getPermissionContext() != null 
                ? graphContext.getPermissionContext().getUserId() : "system";

        return DecisionContext.builder()
                .contextId("dctx_" + UUID.randomUUID().toString().substring(0, 8))
                .graphContext(graphContext)
                .reasoningEvidence(evidence)
                .userId(userId)
                .activeCollections(graphContext != null ? graphContext.getActiveCollections() : Collections.emptySet())
                .confidenceThreshold(graphContext != null ? graphContext.getConfidenceThreshold() : 0.50)
                .objective(DecisionObjective.defaultObjective(evidence != null ? evidence.getObjectiveDescription() : "general_reasoning"))
                .constraints(DecisionConstraints.defaultConstraints())
                .scope(DecisionScope.defaultScope())
                .environmentalSignals(EnvironmentalSignals.defaultSignals())
                .requestMetadata(RequestMetadata.create(null, "WEB_APP"))
                .build();
    }
}
