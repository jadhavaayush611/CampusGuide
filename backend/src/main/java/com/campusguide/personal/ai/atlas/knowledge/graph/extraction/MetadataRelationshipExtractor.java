package com.campusguide.personal.ai.atlas.knowledge.graph.extraction;

import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollection;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.KnowledgeEdge;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipStrength;
import com.campusguide.personal.ai.atlas.knowledge.graph.edge.RelationshipType;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.graph.node.NodeType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Extracts domain entity relationships (TEACHES, ENROLLED_IN, LOCATED_IN, PREREQUISITE, USES, RELATED_TO, PART_OF)
 * from artifact metadata attributes.
 */
@Component
public class MetadataRelationshipExtractor implements RelationshipExtractor {

    @Override
    public String getName() {
        return "metadata-relationship-extractor";
    }

    @Override
    public int getPriority() {
        return 30;
    }

    @Override
    public List<KnowledgeEdge> extractFromArtifact(KnowledgeArtifact artifact) {
        if (artifact == null || artifact.getId() == null || artifact.getMetadata() == null) {
            return Collections.emptyList();
        }

        List<KnowledgeEdge> edges = new ArrayList<>();
        NodeIdentifier artifactId = NodeIdentifier.ofArtifact(artifact.getId());
        Map<String, Object> metaMap = artifact.getMetadata().getAttributes() != null ? artifact.getMetadata().getAttributes() : Map.of();

        // 1. Course & Prerequisite metadata
        String courseCode = getString(metaMap, "courseCode", "course_id", "courseId");
        String prerequisite = getString(metaMap, "prerequisite", "prerequisiteCourse", "prereqCode");
        String instructor = getString(metaMap, "instructor", "instructorId", "teacher");
        String building = getString(metaMap, "building", "buildingId", "location");
        String department = getString(metaMap, "department", "departmentId", "dept");

        if (courseCode != null) {
            NodeIdentifier courseId = NodeIdentifier.of(NodeType.COURSE, courseCode);
            // Artifact RELATED_TO / BELONGS_TO Course
            edges.add(RelationshipBuilder.create()
                    .source(artifactId)
                    .target(courseId)
                    .relationship(RelationshipType.RELATED_TO)
                    .strength(RelationshipStrength.STRONG)
                    .extractor(getName())
                    .provenance("metadata-course")
                    .sourceArtifactId(artifact.getId().getValue())
                    .build());

            if (instructor != null) {
                NodeIdentifier personId = NodeIdentifier.of(NodeType.PERSON, instructor);
                // Person TEACHES Course
                edges.add(RelationshipBuilder.create()
                        .source(personId)
                        .target(courseId)
                        .relationship(RelationshipType.TEACHES)
                        .strength(RelationshipStrength.STRONG)
                        .extractor(getName())
                        .provenance("metadata-instructor-teaches")
                        .build());
            }

            if (prerequisite != null) {
                NodeIdentifier prereqId = NodeIdentifier.of(NodeType.COURSE, prerequisite);
                // PrereqCourse PREREQUISITE Course
                edges.add(RelationshipBuilder.create()
                        .source(prereqId)
                        .target(courseId)
                        .relationship(RelationshipType.PREREQUISITE)
                        .strength(RelationshipStrength.DEFINITIVE)
                        .extractor(getName())
                        .provenance("metadata-prerequisite")
                        .build());
            }
        }

        // 2. Building & Location metadata
        if (building != null) {
            NodeIdentifier buildingId = NodeIdentifier.of(NodeType.BUILDING, building);
            edges.add(RelationshipBuilder.create()
                    .source(artifactId)
                    .target(buildingId)
                    .relationship(RelationshipType.LOCATED_IN)
                    .strength(RelationshipStrength.MEDIUM)
                    .extractor(getName())
                    .provenance("metadata-location")
                    .sourceArtifactId(artifact.getId().getValue())
                    .build());
        }

        // 3. Department & Organization metadata
        if (department != null) {
            NodeIdentifier deptId = NodeIdentifier.of(NodeType.DEPARTMENT, department);
            edges.add(RelationshipBuilder.create()
                    .source(artifactId)
                    .target(deptId)
                    .relationship(RelationshipType.PART_OF)
                    .strength(RelationshipStrength.MEDIUM)
                    .extractor(getName())
                    .provenance("metadata-department")
                    .sourceArtifactId(artifact.getId().getValue())
                    .build());
        }

        return edges;
    }

    @Override
    public List<KnowledgeEdge> extractFromCollection(KnowledgeCollection collection, List<KnowledgeArtifact> artifacts) {
        if (artifacts == null) return Collections.emptyList();
        List<KnowledgeEdge> edges = new ArrayList<>();
        for (KnowledgeArtifact artifact : artifacts) {
            edges.addAll(extractFromArtifact(artifact));
        }
        return edges;
    }

    @Override
    public List<KnowledgeEdge> extractFromEntity(Object entity) {
        if (entity instanceof KnowledgeArtifact artifact) {
            return extractFromArtifact(artifact);
        }
        return Collections.emptyList();
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
