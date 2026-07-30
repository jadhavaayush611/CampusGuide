package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.graph.extension.PlanningReasoningExtension;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjection;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjectionPolicy;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GraphContextTest {

    @Test
    @DisplayName("GraphContext encapsulates root nodes, projection, objective, constraints, and permissions")
    void testGraphContextEncapsulation() {
        NodeIdentifier root = NodeIdentifier.of(NodeType.COURSE, "CS101");
        GraphProjection Policy = GraphProjection.builder().build();
        ReasoningObjective obj = ReasoningObjective.explainRelationship(Set.of(root));
        PermissionContext perm = PermissionContext.admin();

        GraphContext ctx = GraphContext.builder()
                .contextId("gctx_1")
                .rootNodes(Set.of(root))
                .graphView(Policy)
                .objective(obj)
                .activeCollections(Set.of("academic"))
                .permissionContext(perm)
                .maxReasoningDepth(3)
                .confidenceThreshold(0.7)
                .build();

        assertEquals("gctx_1", ctx.getContextId());
        assertTrue(ctx.getRootNodes().contains(root));
        assertEquals(obj, ctx.getObjective());
        assertEquals(perm, ctx.getPermissionContext());
        assertEquals(3, ctx.getMaxReasoningDepth());
        assertEquals(0.7, ctx.getConfidenceThreshold());
    }

    @Test
    @DisplayName("GraphContext supports future extensions without redesigning core structures")
    void testFutureExtensionPoints() {
        GraphContext ctx = GraphContext.builder().contextId("gctx_ext").build();

        PlanningReasoningExtension mockPlanner = (context, goal) -> List.of("Step 1: Inspect Graph", "Step 2: Reason Path");
        ctx.setExtension("planner", mockPlanner);

        assertTrue(ctx.getExtension("planner", PlanningReasoningExtension.class).isPresent());
        PlanningReasoningExtension ext = ctx.getExtension("planner", PlanningReasoningExtension.class).get();
        List<String> plan = ext.generateActionPlan(ctx, "Achieve Goal");
        assertEquals(2, plan.size());
    }
}
