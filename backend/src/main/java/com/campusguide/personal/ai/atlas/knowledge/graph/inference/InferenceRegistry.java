package com.campusguide.personal.ai.atlas.knowledge.graph.inference;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry holding active InferenceRule instances for the InferenceEngine.
 */
@Component
public class InferenceRegistry {

    private final Map<String, InferenceRule> rules = new ConcurrentHashMap<>();

    public InferenceRegistry() {
        // Register default rules
        registerRule(new TransitiveRelationshipRule());
        registerRule(new HierarchicalPrerequisiteRule());
    }

    public void registerRule(InferenceRule rule) {
        if (rule != null && rule.getRuleId() != null) {
            rules.put(rule.getRuleId(), rule);
        }
    }

    public boolean unregisterRule(String ruleId) {
        return rules.remove(ruleId) != null;
    }

    public List<InferenceRule> getRules() {
        return Collections.unmodifiableList(new ArrayList<>(rules.values()));
    }

    public void clear() {
        rules.clear();
    }
}
