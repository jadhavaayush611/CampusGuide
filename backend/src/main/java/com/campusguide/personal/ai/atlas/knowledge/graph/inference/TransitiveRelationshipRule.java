package com.campusguide.personal.ai.atlas.knowledge.graph.inference;

import com.campusguide.personal.ai.atlas.knowledge.graph.edge.EdgeMetadata;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipStrength;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.view.KnowledgeGraphView;

import java.util.ArrayList;
import java.util.List;

/**
 * Infers indirect relationships when A -> B and B -> C share compatible relationship types (e.g. SAME_DEPARTMENT).
 */
public class TransitiveRelationshipRule implements InferenceRule {

    private final RelationshipType targetType;

    public TransitiveRelationshipRule(RelationshipType targetType) {
        this.targetType = targetType != null ? targetType : RelationshipType.SAME_DEPARTMENT;
    }

    public TransitiveRelationshipRule() {
        this(RelationshipType.SAME_DEPARTMENT);
    }

    @Override
    public String getRuleId() {
        return "transitive_rel_" + targetType.name().toLowerCase();
    }

    @Override
    public String getRuleName() {
        return "Transitive Relationship Inference (" + targetType.name() + ")";
    }

    @Override
    public List<KnowledgeEdge> evaluate(KnowledgeGraphView view) {
        List<KnowledgeEdge> inferred = new ArrayList<>();
        if (view == null) return inferred;

        List<KnowledgeNode> nodes = view.getNodes();
        for (KnowledgeNode a : nodes) {
            List<KnowledgeEdge> outA = view.getOutgoingEdges(a.getId());
            for (KnowledgeEdge edgeAB : outA) {
                if (edgeAB.getRelationshipType() == targetType) {
                    NodeIdentifier bId = edgeAB.getTargetNodeId();
                    List<KnowledgeEdge> outB = view.getOutgoingEdges(bId);
                    for (KnowledgeEdge edgeBC : outB) {
                        if (edgeBC.getRelationshipType() == targetType && !edgeBC.getTargetNodeId().equals(a.getId())) {
                            NodeIdentifier cId = edgeBC.getTargetNodeId();

                            // Check if direct edge already exists
                            boolean exists = outA.stream().anyMatch(e -> e.getTargetNodeId().equals(cId) && e.getRelationshipType() == targetType);
                            if (!exists) {
                                String infEdgeId = "inf_trans_" + a.getId().getValue() + "_" + cId.getValue();
                                RelationshipStrength combinedStrength = edgeAB.getStrength().combine(edgeBC.getStrength());

                                EdgeMetadata meta = EdgeMetadata.builder().extractorName("transitive-rule").build();
                                meta.addProperty("confidenceScore", 0.85);

                                KnowledgeEdge infEdge = KnowledgeEdge.builder()
                                        .id(infEdgeId)
                                        .sourceNodeId(a.getId())
                                        .targetNodeId(cId)
                                        .relationshipType(RelationshipType.INFERRED_ACADEMIC_PEER)
                                        .strength(combinedStrength)
                                        .metadata(meta)
                                        .bidirectional(true)
                                        .build();
                                inferred.add(infEdge);
                            }
                        }
                    }
                }
            }
        }
        return inferred;
    }
}
