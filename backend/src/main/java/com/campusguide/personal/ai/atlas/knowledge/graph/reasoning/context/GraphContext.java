package com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context;

import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjection;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjectionPolicy;
import com.campusguide.personal.ai.atlas.knowledge.graph.traversal.TraversalPolicy;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Primary context object encapsulating graph projections, root nodes, objective, bounds, active collections,
 * permissions, and future expansion extension attributes.
 */
@Data
@Builder
public class GraphContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String contextId;
    private final Set<NodeIdentifier> rootNodes;
    private final KnowledgeGraphView graphView; // Reasoning engine operates exclusively on KnowledgeGraphView
    private final GraphProjectionPolicy projectionPolicy;
    private final TraversalPolicy traversalPolicy;
    private final ReasoningObjective objective;
    private final ReasoningConstraints constraints;
    private final ReasoningScope scope;
    private final Set<String> activeCollections;
    private final PermissionContext permissionContext;
    private final int maxReasoningDepth;
    private final double confidenceThreshold;

    @Builder.Default
    private final Map<String, Object> extensionAttributes = new ConcurrentHashMap<>();

    public GraphContext(String contextId,
                        Set<NodeIdentifier> rootNodes,
                        KnowledgeGraphView graphView,
                        GraphProjectionPolicy projectionPolicy,
                        TraversalPolicy traversalPolicy,
                        ReasoningObjective objective,
                        ReasoningConstraints constraints,
                        ReasoningScope scope,
                        Set<String> activeCollections,
                        PermissionContext permissionContext,
                        int maxReasoningDepth,
                        double confidenceThreshold,
                        Map<String, Object> extensionAttributes) {
        this.contextId = contextId != null ? contextId : "gctx_" + UUID.randomUUID().toString().substring(0, 8);
        this.rootNodes = rootNodes != null ? new HashSet<>(rootNodes) : Collections.emptySet();
        this.graphView = graphView;
        this.projectionPolicy = projectionPolicy != null ? projectionPolicy : GraphProjectionPolicy.permissive();
        this.traversalPolicy = traversalPolicy != null ? traversalPolicy : TraversalPolicy.defaultPolicy();
        this.objective = objective != null ? objective : ReasoningObjective.contextGeneration();
        this.constraints = constraints != null ? constraints : ReasoningConstraints.defaultConstraints();
        this.scope = scope != null ? scope : ReasoningScope.defaultScope();
        this.activeCollections = activeCollections != null ? new HashSet<>(activeCollections) : Collections.emptySet();
        this.permissionContext = permissionContext != null ? permissionContext : PermissionContext.admin();
        this.maxReasoningDepth = maxReasoningDepth > 0 ? maxReasoningDepth : this.constraints.getMaxDepth();
        this.confidenceThreshold = confidenceThreshold >= 0.0 ? confidenceThreshold : this.constraints.getConfidenceThreshold();
        this.extensionAttributes = new ConcurrentHashMap<>();
        if (extensionAttributes != null) {
            this.extensionAttributes.putAll(extensionAttributes);
        }
    }

    public GraphProjection getGraphProjection() {
        if (graphView instanceof GraphProjection proj) {
            return proj;
        }
        return null;
    }

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
}
