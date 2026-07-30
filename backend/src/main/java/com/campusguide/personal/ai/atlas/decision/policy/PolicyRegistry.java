package com.campusguide.personal.ai.atlas.decision.policy;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for managing active policy rules and decision policies.
 */
@Component
public class PolicyRegistry {

    private final Map<String, PolicyRule> rules = new ConcurrentHashMap<>();

    public PolicyRegistry(List<PolicyRule> initialRules) {
        if (initialRules != null) {
            for (PolicyRule rule : initialRules) {
                registerRule(rule);
            }
        }
    }

    public void registerRule(PolicyRule rule) {
        if (rule != null && rule.getRuleId() != null) {
            rules.put(rule.getRuleId(), rule);
        }
    }

    public void unregisterRule(String ruleId) {
        if (ruleId != null) {
            rules.remove(ruleId);
        }
    }

    public List<PolicyRule> getActiveRules() {
        return rules.values().stream()
                .sorted(Comparator.comparingInt(PolicyRule::getPriority))
                .collect(Collectors.toList());
    }
}
