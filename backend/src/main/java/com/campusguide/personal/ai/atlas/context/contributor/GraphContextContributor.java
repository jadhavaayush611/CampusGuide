package com.campusguide.personal.ai.atlas.context.contributor;

import com.campusguide.personal.ai.atlas.context.ContextContributor;
import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceScore;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceSource;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceType;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjection;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjectionEngine;
import com.campusguide.personal.ai.atlas.knowledge.graph.projection.GraphProjectionPolicy;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.PermissionContext;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.ReasoningObjective;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEngine;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Context Contributor bridging GraphContext -> ReasoningEngine -> ReasoningEvidence -> Context Intelligence Layer.
 */
@Component
@Slf4j
public class GraphContextContributor implements ContextContributor {

    private final GraphProjectionEngine projectionEngine;
    private final ReasoningEngine reasoningEngine;

    @Autowired
    public GraphContextContributor(@Autowired(required = false) GraphProjectionEngine projectionEngine,
                                   @Autowired(required = false) ReasoningEngine reasoningEngine) {
        this.projectionEngine = projectionEngine != null ? projectionEngine : new GraphProjectionEngine();
        this.reasoningEngine = reasoningEngine != null ? reasoningEngine : new ReasoningEngine();
    }

    public GraphContextContributor() {
        this(new GraphProjectionEngine(), new ReasoningEngine());
    }

    @Override
    public String getName() {
        return "graphReasoning";
    }

    @Override
    public void contribute(AtlasChatRequest request, AtlasContext context) {
        if (context == null) return;

        try {
            // Create a baseline graph projection
            KnowledgeGraph baseGraph = KnowledgeGraph.create("atlas_global_graph");
            GraphProjectionPolicy policy = GraphProjectionPolicy.permissive();
            GraphProjection projection = projectionEngine.project(baseGraph, Collections.emptySet(), policy);

            // Construct GraphContext
            GraphContext graphContext = GraphContext.builder()
                    .contextId("gctx_" + context.getConversationId())
                    .graphView(projection)
                    .projectionPolicy(policy)
                    .objective(ReasoningObjective.contextGeneration())
                    .permissionContext(PermissionContext.admin())
                    .maxReasoningDepth(3)
                    .confidenceThreshold(0.5)
                    .build();

            // Execute reasoning
            ReasoningEvidence reasoningEvidence = reasoningEngine.reason(graphContext);

            // Integrate with Context Intelligence Layer via EvidenceBundle
            RetrievalEvidence ev = RetrievalEvidence.builder()
                    .type(EvidenceType.CAMPUS_KNOWLEDGE)
                    .source(EvidenceSource.KNOWLEDGE_BASE)
                    .entityKey("graph_reasoning_bundle")
                    .contentSnippet(reasoningEvidence.getReasoningSummaryText())
                    .rationale("Graph reasoning evidence generated for Context Intelligence Layer")
                    .score(EvidenceScore.builder()
                            .relevanceScore(0.92)
                            .confidenceScore(reasoningEvidence.getConfidence())
                            .sourceAuthorityScore(0.90)
                            .qualityScore(0.90)
                            .build())
                    .build();
            ev.getScore().calculateOverallScore();

            EvidenceBundle bundle = EvidenceBundle.builder()
                    .targetDomain("graphReasoning")
                    .evidences(List.of(ev))
                    .confidence(reasoningEvidence.getConfidence())
                    .sourceSummary(reasoningEvidence.getReasoningSummaryText())
                    .build();

            context.addEvidenceBundle(bundle);
            context.addContribution(getName(), reasoningEvidence);

            log.debug("GraphContextContributor enriched AtlasContext with evidenceId={}", reasoningEvidence.getEvidenceId());
        } catch (Exception e) {
            log.error("Failed to execute GraphContextContributor: {}", e.getMessage(), e);
        }
    }
}
