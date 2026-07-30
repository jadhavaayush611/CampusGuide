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
 * Infers indirect prerequisites across multi-hop course/academic dependency chains (A -> B requires, B -> C requires => A indirect prerequisite C).
 */
public class HierarchicalPrerequisiteRule implements InferenceRule {

    @Override
    public String getRuleId() {
        return "hierarchical_prerequisite_rule";
    }

    @Override
    public String getRuleName() {
        return "Hierarchical Prerequisite Inference";
    }

    @Override
    public List<KnowledgeEdge> evaluate(KnowledgeGraphView view) {
        List<KnowledgeEdge> inferred = new ArrayList<>();
        if (view == null) return inferred;

        for (KnowledgeNode a : view.getNodes()) {
            for (KnowledgeEdge ab : view.getOutgoingEdges(a.getId())) {
                if (ab.getRelationshipType() == RelationshipType.PREREQUISITE_FOR || ab.getRelationshipType() == RelationshipType.REQUIRES) {
                    NodeIdentifier bId = ab.getTargetNodeId();
                    for (KnowledgeEdge bc : view.getOutgoingEdges(bId)) {
                        if (bc.getRelationshipType() == RelationshipType.PREREQUISITE_FOR || bc.getRelationshipType() == RelationshipType.REQUIRES) {
                            NodeIdentifier cId = bc.getTargetNodeId();
                            if (!cId.equals(a.getId())) {
                                String edgeId = "inf_prereq_" + a.getId().getValue() + "_" + cId.getValue();
                                EdgeMetadata meta = EdgeMetadata.builder().extractorName("prereq-rule").build();
                                meta.addProperty("confidenceScore", 0.90);

                                KnowledgeEdge infEdge = KnowledgeEdge.builder()
                                        .id(edgeId)
                                        .sourceNodeId(a.getId())
                                        .targetNodeId(cId)
                                        .relationshipType(RelationshipType.INDIRECT_PREREQUISITE)
                                        .strength(RelationshipStrength.of(0.90))
                                        .metadata(meta)
                                        .bidirectional(false)
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
