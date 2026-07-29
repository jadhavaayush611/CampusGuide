package com.campusguide.personal.ai.atlas.knowledge.ranking;

import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Multi-dimensional score breakdown encapsulating ranking metrics for a KnowledgeArtifact.
 * Implements deterministic ordering: totalScore desc, collectionPriority desc, artifactId asc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactScore implements Serializable, Comparable<ArtifactScore> {

    private static final long serialVersionUID = 1L;

    private KnowledgeArtifact artifact;

    private double totalScore;

    private double semanticSimilarity;
    private double keywordOverlap;
    private double freshnessScore;
    private double evidenceQuality;
    private double sourceAuthority;
    private double collectionPriority;
    private double retrievalConfidence;

    private String explanation;

    @Override
    public int compareTo(ArtifactScore o) {
        if (o == null) return -1;
        int cmp = Double.compare(o.totalScore, this.totalScore);
        if (cmp != 0) return cmp;

        int collCmp = Double.compare(o.collectionPriority, this.collectionPriority);
        if (collCmp != 0) return collCmp;

        String id1 = this.artifact != null && this.artifact.getId() != null ? this.artifact.getId().getValue() : "";
        String id2 = o.artifact != null && o.artifact.getId() != null ? o.artifact.getId().getValue() : "";
        return id1.compareTo(id2);
    }
}
