package com.campusguide.personal.ai.atlas.decision.policy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * High-level policy encapsulating a set of PolicyRules.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    private String policyId;
    private String policyName;
    private String description;
    private boolean active;

    @Builder.Default
    private List<PolicyRule> rules = new ArrayList<>();

    public void addRule(PolicyRule rule) {
        if (rule != null) {
            rules.add(rule);
        }
    }
}
