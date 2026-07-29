package com.campusguide.personal.ai.atlas.context.retrieval;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetrievalPolicyTest {

    @Test
    @DisplayName("Should initialize default retrieval policy configuration")
    void testRetrievalPolicy_Defaults() {
        RetrievalPolicy policy = new RetrievalPolicy();

        assertEquals(0.45, policy.getMinConfidenceThreshold());
        assertTrue(policy.isAlwaysRetrieveUserProfile());
        assertTrue(policy.isEnableFallbackToAllIfLowConfidence());
        assertEquals(5, policy.getMaxStrategies());
    }

    @Test
    @DisplayName("Should allow custom retrieval policy configuration")
    void testRetrievalPolicy_Custom() {
        RetrievalPolicy policy = RetrievalPolicy.builder()
                .minConfidenceThreshold(0.60)
                .alwaysRetrieveUserProfile(false)
                .enableFallbackToAllIfLowConfidence(false)
                .maxStrategies(3)
                .build();

        assertEquals(0.60, policy.getMinConfidenceThreshold());
        assertFalse(policy.isAlwaysRetrieveUserProfile());
        assertFalse(policy.isEnableFallbackToAllIfLowConfidence());
        assertEquals(3, policy.getMaxStrategies());
    }
}
