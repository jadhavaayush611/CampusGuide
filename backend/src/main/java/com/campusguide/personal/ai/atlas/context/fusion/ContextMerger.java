package com.campusguide.personal.ai.atlas.context.fusion;

import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Handles context merging, duplicate removal, evidence preservation, and deterministic ordering.
 */
@Component
@Slf4j
public class ContextMerger {

    /**
     * Merges multiple evidence bundles for the same target domain into a unified bundle,
     * removing exact duplicate evidence while preserving unique evidence and deterministic ordering.
     */
    public EvidenceBundle mergeBundles(List<EvidenceBundle> bundles, String targetDomain) {
        if (bundles == null || bundles.isEmpty()) {
            return EvidenceBundle.builder()
                    .targetDomain(targetDomain)
                    .evidences(Collections.emptyList())
                    .sourceSummary("Empty bundle")
                    .build();
        }

        List<RetrievalEvidence> allEvidences = new ArrayList<>();
        Set<String> seenSnippets = new TreeSet<>();
        List<FusionDecision> decisions = new ArrayList<>();

        for (EvidenceBundle bundle : bundles) {
            if (bundle == null || bundle.getEvidences() == null) continue;
            for (RetrievalEvidence ev : bundle.getEvidences()) {
                if (ev == null || ev.getContentSnippet() == null) continue;
                String snippet = ev.getContentSnippet().trim();

                if (seenSnippets.contains(snippet)) {
                    decisions.add(FusionDecision.builder()
                            .key(ev.getEntityKey() != null ? ev.getEntityKey() : snippet)
                            .targetDomain(targetDomain)
                            .action("DEDUPLICATED")
                            .details("Removed duplicate evidence snippet")
                            .build());
                } else {
                    seenSnippets.add(snippet);
                    allEvidences.add(ev);
                }
            }
        }

        // Deterministic ordering: By overall score desc, then snippet content asc
        allEvidences.sort(Comparator
                .comparing((RetrievalEvidence e) -> e.getScore() != null ? e.getScore().getOverallScore() : 0.0, Comparator.reverseOrder())
                .thenComparing(e -> e.getContentSnippet() != null ? e.getContentSnippet() : ""));

        EvidenceBundle mergedBundle = EvidenceBundle.builder()
                .targetDomain(targetDomain)
                .evidences(allEvidences)
                .sourceSummary(String.format("Fused %d unique evidence entries across %d bundles", allEvidences.size(), bundles.size()))
                .timestamp(System.currentTimeMillis())
                .build();
        mergedBundle.recalculateAggregateScore();
        return mergedBundle;
    }

    /**
     * Merges placeholders maps while removing duplicate keys and preserving deterministic key ordering.
     */
    public Map<String, Object> mergePlaceholders(Map<String, Object> existing, Map<String, Object> newPlaceholders, List<FusionDecision> decisions) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (existing != null) {
            merged.putAll(existing);
        }
        if (newPlaceholders != null) {
            newPlaceholders.forEach((k, v) -> {
                if (merged.containsKey(k)) {
                    Object existingVal = merged.get(k);
                    if (existingVal != null && existingVal.equals(v)) {
                        if (decisions != null) {
                            decisions.add(FusionDecision.builder()
                                    .key(k)
                                    .action("DEDUPLICATED")
                                    .details("Duplicate placeholder value ignored")
                                    .build());
                        }
                    } else {
                        // Merge or replace
                        merged.put(k, v);
                        if (decisions != null) {
                            decisions.add(FusionDecision.builder()
                                    .key(k)
                                    .action("MERGED")
                                    .details(String.format("Updated placeholder '%s' with new value", k))
                                    .build());
                        }
                    }
                } else {
                    merged.put(k, v);
                }
            });
        }
        return merged;
    }
}
