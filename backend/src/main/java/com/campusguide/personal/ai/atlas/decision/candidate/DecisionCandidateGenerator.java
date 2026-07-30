package com.campusguide.personal.ai.atlas.decision.candidate;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generator component that runs registered CandidateStrategies against DecisionContext.
 */
@Slf4j
@Component
public class DecisionCandidateGenerator {

    private final CandidateRegistry registry;

    public DecisionCandidateGenerator(CandidateRegistry registry) {
        this.registry = registry;
    }

    public List<DecisionCandidate> generateCandidates(DecisionContext context) {
        if (context == null) {
            log.warn("Cannot generate candidates for null DecisionContext");
            return List.of(DecisionCandidate.simple("cand_fallback", "FALLBACK_RESPONSE", "Null context", 0.0));
        }

        List<CandidateStrategy> strategies = registry.getRegisteredStrategies();
        List<DecisionCandidate> allCandidates = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (CandidateStrategy strategy : strategies) {
            try {
                if (strategy.supports(context)) {
                    List<DecisionCandidate> generated = strategy.generateCandidates(context);
                    if (generated != null) {
                        for (DecisionCandidate cand : generated) {
                            if (cand != null && cand.getCandidateId() != null && seenIds.add(cand.getCandidateId())) {
                                allCandidates.add(cand);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error executing candidate strategy: {}", strategy.getStrategyId(), e);
            }
        }

        int maxCandidates = context.getConstraints() != null ? context.getConstraints().getMaxCandidates() : 10;
        if (allCandidates.size() > maxCandidates) {
            return allCandidates.subList(0, maxCandidates);
        }

        log.debug("Generated {} candidate decision options for contextId={}", allCandidates.size(), context.getContextId());
        return allCandidates;
    }
}
