package com.campusguide.personal.ai.atlas.context.fusion;

import com.campusguide.personal.ai.atlas.context.evidence.EvidenceSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Audit record capturing details of a resolved context conflict.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConflictResolution {
    private String entityKey;
    private String targetDomain;
    private Object winningValue;
    private Object losingValue;
    private EvidenceSource winningSource;
    private EvidenceSource losingSource;
    private double winningScore;
    private double losingScore;
    private String resolutionReason;
    @Builder.Default
    private long timestamp = System.currentTimeMillis();
}
