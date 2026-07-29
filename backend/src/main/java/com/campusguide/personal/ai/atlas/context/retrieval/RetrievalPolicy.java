package com.campusguide.personal.ai.atlas.context.retrieval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Policy rules governing intelligent strategy selection and fallback behavior.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Component
public class RetrievalPolicy {

    @Builder.Default
    private double minConfidenceThreshold = 0.45;

    @Builder.Default
    private boolean alwaysRetrieveUserProfile = true;

    @Builder.Default
    private boolean enableFallbackToAllIfLowConfidence = true;

    @Builder.Default
    private int maxStrategies = 5;
}
