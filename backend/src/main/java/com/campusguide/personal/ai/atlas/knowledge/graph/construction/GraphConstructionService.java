package com.campusguide.personal.ai.atlas.knowledge.graph.construction;

import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.extraction.RelationshipRegistry;
import com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle.GraphDiagnostics;
import com.campusguide.personal.ai.atlas.knowledge.graph.lifecycle.GraphLifecycleState;
import com.campusguide.personal.ai.atlas.knowledge.graph.metrics.KnowledgeGraphMetrics;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraph;
import com.campusguide.personal.ai.atlas.knowledge.graph.model.KnowledgeGraphMetadata;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeAttributes;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.KnowledgeNode;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service orchestrating Knowledge Graph construction from KnowledgeArtifacts and KnowledgeCollections.
 * Handles node generation, entity node synthesis, edge extraction, deduplication, merging, and consistency validation.
 */
@Service
public class GraphConstructionService {

    private static final Logger log = LoggerFactory.getLogger(GraphConstructionService.class);

    private final RelationshipRegistry relationshipRegistry;
    private final KnowledgeGraphMetrics graphMetrics;

    public GraphConstructionService(RelationshipRegistry relationshipRegistry, KnowledgeGraphMetrics graphMetrics) {
        this.relationshipRegistry = relationshipRegistry;
        this.graphMetrics = graphMetrics;
    }

    /**
     * Builds a KnowledgeGraph from a list of KnowledgeArtifacts.
     */
    public KnowledgeGraph buildGraphFromArtifacts(String graphId, List<KnowledgeArtifact> artifacts) {
        long startTime = System.currentTimeMillis();
        log.info("Starting Knowledge Graph construction for graphId: {}, artifact count: {}", graphId, artifacts != null ? artifacts.size() : 0);

        KnowledgeGraphBuilder builder = KnowledgeGraphBuilder.builder()
                .graphId(graphId)
                .name("Graph-" + graphId);

        if (artifacts == null || artifacts.isEmpty()) {
            KnowledgeGraph graph = builder.build();
            graph.getMetadata().setLifecycleState(GraphLifecycleState.ACTIVE);
            return graph;
        }

        int createdNodes = 0;
        int mergedNodes = 0;

        // 1. Synthesize nodes & extract relationships for each artifact
        for (KnowledgeArtifact artifact : artifacts) {
            if (artifact == null || artifact.getId() == null) continue;

            // Primary Artifact Node
            KnowledgeNode artifactNode = createArtifactNode(artifact);
            builder.addNode(artifactNode);
            createdNodes++;

            // Create domain entity nodes from metadata (Course, Person, Building, etc.)
            List<KnowledgeNode> entityNodes = extractEntityNodes(artifact);
            for (KnowledgeNode entityNode : entityNodes) {
                builder.addNode(entityNode);
                createdNodes++;
            }

            // Extract explicit & structural edges
            List<KnowledgeEdge> extractedEdges = relationshipRegistry.extractFromArtifact(artifact);
            builder.addEdges(extractedEdges);

            if (artifact.getCollectionId() != null) {
                builder.addSourceCollection(artifact.getCollectionId());
            }
        }

        KnowledgeGraph graph = builder.build();
        long duration = System.currentTimeMillis() - startTime;

        // Record diagnostics
        GraphDiagnostics diagnostics = graph.getMetadata().getDiagnostics();
        diagnostics.setBuildDurationMs(duration);
        diagnostics.setNodesCreated(createdNodes);

        log.info("Knowledge Graph construction completed for graphId: {} in {} ms. Nodes: {}, Edges: {}",
                graphId, duration, graph.getMetadata().getNodeCount(), graph.getMetadata().getEdgeCount());

        if (graphMetrics != null) {
            graphMetrics.recordGraphBuild(duration, graph.getMetadata().getNodeCount(), graph.getMetadata().getEdgeCount());
        }

        return graph;
    }

    /**
     * Builds a KnowledgeGraph from a KnowledgeCollection and its artifacts.
     */
    public KnowledgeGraph buildGraphFromCollection(String graphId, KnowledgeCollection collection, List<KnowledgeArtifact> artifacts) {
        long startTime = System.currentTimeMillis();

        String effectiveGraphId = (graphId != null && !graphId.isBlank()) ? graphId : 
                (collection != null && collection.getCollectionId() != null ? "graph_" + collection.getCollectionId() : "graph_unknown");

        log.info("Building Knowledge Graph from collectionId: {}, graphId: {}", 
                collection != null && collection.getCollectionId() != null ? collection.getCollectionId() : "null", effectiveGraphId);

        KnowledgeGraphBuilder builder = KnowledgeGraphBuilder.builder()
                .graphId(effectiveGraphId)
                .name(collection != null ? collection.getName() : effectiveGraphId);

        if (collection != null && collection.getCollectionId() != null) {
            builder.addSourceCollection(collection.getCollectionId());

            // Collection Node
            KnowledgeNode collectionNode = KnowledgeNode.builder()
                    .id(NodeIdentifier.ofCollection(collection.getCollectionId()))
                    .type(NodeType.KNOWLEDGE_COLLECTION)
                    .name(collection.getName() != null ? collection.getName() : collection.getCollectionId())
                    .sourceCollectionId(collection.getCollectionId())
                    .provenance("collection-definition")
                    .attributes(NodeAttributes.of(Map.of("scope", collection.getScope() != null ? collection.getScope().name() : "DEFAULT")))
                    .build();

            builder.addNode(collectionNode);
        }

        // Add artifact nodes & relationships
        if (artifacts != null) {
            for (KnowledgeArtifact artifact : artifacts) {
                if (artifact == null || artifact.getId() == null) continue;

                KnowledgeNode artifactNode = createArtifactNode(artifact);
                builder.addNode(artifactNode);

                List<KnowledgeNode> entityNodes = extractEntityNodes(artifact);
                builder.addNodes(entityNodes);

                List<KnowledgeEdge> artifactEdges = relationshipRegistry.extractFromArtifact(artifact);
                builder.addEdges(artifactEdges);
            }

            // Extract collection level membership edges
            List<KnowledgeEdge> collectionEdges = relationshipRegistry.extractFromCollection(collection, artifacts);
            builder.addEdges(collectionEdges);
        }

        KnowledgeGraph graph = builder.build();
        long duration = System.currentTimeMillis() - startTime;

        graph.getMetadata().getDiagnostics().setBuildDurationMs(duration);

        log.info("Collection Knowledge Graph completed for graphId: {} in {} ms. Nodes: {}, Edges: {}",
                effectiveGraphId, duration, graph.getMetadata().getNodeCount(), graph.getMetadata().getEdgeCount());

        if (graphMetrics != null) {
            graphMetrics.recordGraphBuild(duration, graph.getMetadata().getNodeCount(), graph.getMetadata().getEdgeCount());
        }

        return graph;
    }

    private KnowledgeNode createArtifactNode(KnowledgeArtifact artifact) {
        String title = artifact.getMetadata() != null ? artifact.getMetadata().getName() : null;
        if (title == null || title.isBlank()) {
            title = artifact.getId().getValue();
        }

        NodeAttributes attributes = new NodeAttributes();
        if (artifact.getMetadata() != null && artifact.getMetadata().getAttributes() != null) {
            attributes.merge(NodeAttributes.of(artifact.getMetadata().getAttributes()));
        }
        attributes.put("type", artifact.getType() != null ? artifact.getType().name() : "DOCUMENT");

        return KnowledgeNode.builder()
                .id(NodeIdentifier.ofArtifact(artifact.getId()))
                .type(NodeType.KNOWLEDGE_ARTIFACT)
                .name(title)
                .attributes(attributes)
                .sourceArtifactId(artifact.getId().getValue())
                .sourceCollectionId(artifact.getCollectionId())
                .provenance("artifact-ingestion")
                .createdAt(artifact.getCreatedAt() != null ? artifact.getCreatedAt() : Instant.now())
                .updatedAt(artifact.getUpdatedAt() != null ? artifact.getUpdatedAt() : Instant.now())
                .build();
    }

    private List<KnowledgeNode> extractEntityNodes(KnowledgeArtifact artifact) {
        List<KnowledgeNode> nodes = new ArrayList<>();
        if (artifact == null || artifact.getMetadata() == null) return nodes;

        Map<String, Object> metaMap = artifact.getMetadata().getAttributes() != null ? artifact.getMetadata().getAttributes() : Map.of();

        // Course entity node
        String courseCode = getString(metaMap, "courseCode", "course_id", "courseId");
        if (courseCode != null) {
            String courseName = getString(metaMap, "courseName", "title");
            nodes.add(KnowledgeNode.builder()
                    .id(NodeIdentifier.of(NodeType.COURSE, courseCode))
                    .type(NodeType.COURSE)
                    .name(courseName != null ? courseName : courseCode)
                    .attributes(NodeAttributes.of(Map.of("code", courseCode)))
                    .provenance("metadata-extraction")
                    .sourceArtifactId(artifact.getId().getValue())
                    .build());
        }

        // Person / Instructor entity node
        String instructor = getString(metaMap, "instructor", "instructorId", "teacher");
        if (instructor != null) {
            nodes.add(KnowledgeNode.builder()
                    .id(NodeIdentifier.of(NodeType.PERSON, instructor))
                    .type(NodeType.PERSON)
                    .name(instructor)
                    .attributes(NodeAttributes.of(Map.of("personId", instructor)))
                    .provenance("metadata-extraction")
                    .sourceArtifactId(artifact.getId().getValue())
                    .build());
        }

        // Building / Location entity node
        String building = getString(metaMap, "building", "buildingId", "location");
        if (building != null) {
            nodes.add(KnowledgeNode.builder()
                    .id(NodeIdentifier.of(NodeType.BUILDING, building))
                    .type(NodeType.BUILDING)
                    .name(building)
                    .attributes(NodeAttributes.of(Map.of("buildingCode", building)))
                    .provenance("metadata-extraction")
                    .sourceArtifactId(artifact.getId().getValue())
                    .build());
        }

        // Department entity node
        String department = getString(metaMap, "department", "departmentId", "dept");
        if (department != null) {
            nodes.add(KnowledgeNode.builder()
                    .id(NodeIdentifier.of(NodeType.DEPARTMENT, department))
                    .type(NodeType.DEPARTMENT)
                    .name(department)
                    .attributes(NodeAttributes.of(Map.of("deptCode", department)))
                    .provenance("metadata-extraction")
                    .sourceArtifactId(artifact.getId().getValue())
                    .build());
        }

        return nodes;
    }

    private String getString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null && !val.toString().isBlank()) {
                return val.toString().trim();
            }
        }
        return null;
    }
}
