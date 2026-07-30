package com.campusguide.personal.ai.atlas.knowledge.graph.edge;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * Represents the strength or confidence weight of a KnowledgeEdge (between 0.0 and 1.0).
 */
@Getter
@EqualsAndHashCode
public class RelationshipStrength implements Serializable, Comparable<RelationshipStrength> {

    private static final long serialVersionUID = 1L;

    public static final RelationshipStrength WEAK = new RelationshipStrength(0.25);
    public static final RelationshipStrength MEDIUM = new RelationshipStrength(0.50);
    public static final RelationshipStrength STRONG = new RelationshipStrength(0.75);
    public static final RelationshipStrength DEFINITIVE = new RelationshipStrength(1.00);

    private final double score;

    public RelationshipStrength(double score) {
        if (Double.isNaN(score)) {
            this.score = 0.50;
        } else {
            this.score = Math.max(0.0, Math.min(1.0, score));
        }
    }

    public static RelationshipStrength of(double weight) {
        return new RelationshipStrength(weight);
    }

    public RelationshipStrength combine(RelationshipStrength other) {
        if (other == null) return this;
        // Max weight combination rule
        return new RelationshipStrength(Math.max(this.score, other.score));
    }

    @Override
    public int compareTo(RelationshipStrength o) {
        return Double.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return String.format("%.2f", score);
    }
}
