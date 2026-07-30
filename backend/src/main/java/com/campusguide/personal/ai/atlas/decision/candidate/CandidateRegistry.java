package com.campusguide.personal.ai.atlas.decision.candidate;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for managing pluggable candidate strategies.
 */
@Component
public class CandidateRegistry {

    private final Map<String, CandidateStrategy> strategies = new ConcurrentHashMap<>();

    public CandidateRegistry(List<CandidateStrategy> strategyList) {
        if (strategyList != null) {
            for (CandidateStrategy strategy : strategyList) {
                registerStrategy(strategy);
            }
        }
    }

    public void registerStrategy(CandidateStrategy strategy) {
        if (strategy != null && strategy.getStrategyId() != null) {
            strategies.put(strategy.getStrategyId(), strategy);
        }
    }

    public void unregisterStrategy(String strategyId) {
        if (strategyId != null) {
            strategies.remove(strategyId);
        }
    }

    public List<CandidateStrategy> getRegisteredStrategies() {
        return strategies.values().stream()
                .sorted(Comparator.comparingInt(CandidateStrategy::getOrder))
                .collect(Collectors.toList());
    }

    public CandidateStrategy getStrategy(String strategyId) {
        return strategies.get(strategyId);
    }
}
