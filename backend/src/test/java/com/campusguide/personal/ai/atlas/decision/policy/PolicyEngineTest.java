package com.campusguide.personal.ai.atlas.decision.policy;

import com.campusguide.personal.ai.atlas.decision.context.DecisionConstraints;
import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.context.DecisionScope;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PolicyEngineTest {

    private PolicyRegistry registry;
    private DecisionPolicyEngine policyEngine;

    @BeforeEach
    void setUp() {
        PermissionPolicyRule permRule = new PermissionPolicyRule();
        SafetyPolicyRule safetyRule = new SafetyPolicyRule();
        UserPreferencePolicyRule prefRule = new UserPreferencePolicyRule();

        registry = new PolicyRegistry(List.of(permRule, safetyRule, prefRule));
        policyEngine = new DecisionPolicyEngine(registry);
    }

    @Test
    @DisplayName("PolicyEngine permits compliant candidate when permissions and scope are satisfied")
    void testPolicyEngineCompliantCandidate() {
        DecisionCandidate candidate = DecisionCandidate.simple("cand_1", "DIRECT_ANSWER", "Valid candidate", 0.85);

        DecisionContext context = DecisionContext.builder()
                .permissions(Set.of("READ_COURSE", "EXECUTE_ACTION"))
                .constraints(DecisionConstraints.builder().requiredPermissions(Set.of("READ_COURSE")).build())
                .scope(DecisionScope.builder().restrictedActionTypes(Set.of("DELETE_ACCOUNT")).build())
                .build();

        PolicyComplianceResult result = policyEngine.evaluatePolicies(List.of(candidate), context);

        assertNotNull(result);
        assertTrue(result.isFullyCompliant());
        assertEquals(1.0, result.getComplianceScore(candidate.getCandidateId()));
    }

    @Test
    @DisplayName("PolicyEngine denies candidate missing required permissions")
    void testPolicyEngineMissingPermissionDenial() {
        DecisionCandidate candidate = DecisionCandidate.simple("cand_1", "ADMIN_ACTION", "Admin candidate", 0.90);

        DecisionContext context = DecisionContext.builder()
                .permissions(Set.of("READ_COURSE"))
                .constraints(DecisionConstraints.builder().requiredPermissions(Set.of("ADMIN_WRITE")).build())
                .build();

        PolicyComplianceResult result = policyEngine.evaluatePolicies(List.of(candidate), context);

        assertNotNull(result);
        assertFalse(result.isFullyCompliant());
        assertEquals(0.0, result.getComplianceScore(candidate.getCandidateId()));

        List<PolicyEvaluationResult> evals = result.getCandidateEvaluations().get(candidate.getCandidateId());
        assertTrue(evals.stream().anyMatch(e -> e.getStatus() == PolicyEvaluationResult.Status.DENIED));
    }

    @Test
    @DisplayName("PolicyEngine denies candidate matching restricted action type in scope")
    void testPolicyEngineRestrictedActionTypeDenial() {
        DecisionCandidate restrictedCand = DecisionCandidate.simple("cand_restricted", "RESTRICTED_ACTION", "Restricted", 0.90);

        DecisionContext context = DecisionContext.builder()
                .scope(DecisionScope.builder().restrictedActionTypes(Set.of("RESTRICTED_ACTION")).build())
                .build();

        PolicyComplianceResult result = policyEngine.evaluatePolicies(List.of(restrictedCand), context);

        assertFalse(result.isFullyCompliant());
        assertEquals(0.0, result.getComplianceScore(restrictedCand.getCandidateId()));
    }
}
