package com.campusguide.personal.ai.atlas.context.fusion;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ContextFusionEngine orchestrates evidence analysis, duplicate removal, overlapping context merging,
 * evidence preservation, and conflict resolution across retrieved evidence bundles and AtlasContext fields.
 */
@Component
@Slf4j
public class ContextFusionEngine {

    @Getter
    private final ContextMerger merger;
    @Getter
    private final ConflictResolver conflictResolver;

    @Autowired
    public ContextFusionEngine(ContextMerger merger, ConflictResolver conflictResolver) {
        this.merger = merger != null ? merger : new ContextMerger();
        this.conflictResolver = conflictResolver != null ? conflictResolver : new ConflictResolver();
    }

    public ContextFusionEngine() {
        this.merger = new ContextMerger();
        this.conflictResolver = new ConflictResolver();
    }

    /**
     * Fuses evidence bundles in AtlasContext, resolves conflicts, and deduplicates evidence and placeholders.
     */
    public ContextFusionResult fuse(AtlasContext context, List<EvidenceBundle> rawBundles) {
        List<FusionDecision> fusionDecisions = new ArrayList<>();
        List<ConflictResolution> conflictResolutions = new ArrayList<>();

        if (rawBundles == null || rawBundles.isEmpty()) {
            return new ContextFusionResult(Collections.emptyMap(), fusionDecisions, conflictResolutions);
        }

        // Group bundles by domain
        Map<String, List<EvidenceBundle>> bundlesByDomain = new HashMap<>();
        for (EvidenceBundle bundle : rawBundles) {
            if (bundle == null || bundle.getTargetDomain() == null) continue;
            bundlesByDomain.computeIfAbsent(bundle.getTargetDomain(), k -> new ArrayList<>()).add(bundle);
        }

        Map<String, EvidenceBundle> fusedBundles = new HashMap<>();

        // Merge each domain's bundles
        bundlesByDomain.forEach((domain, bundles) -> {
            EvidenceBundle fusedDomainBundle = merger.mergeBundles(bundles, domain);

            // Check for evidence entity conflicts within domain
            Map<String, RetrievalEvidence> entityMap = new HashMap<>();
            List<RetrievalEvidence> finalEvidences = new ArrayList<>();

            for (RetrievalEvidence ev : fusedDomainBundle.getEvidences()) {
                String key = ev.getEntityKey();
                if (key != null && entityMap.containsKey(key)) {
                    RetrievalEvidence existingEv = entityMap.get(key);
                    // Resolve conflict
                    ConflictResolution resolution = conflictResolver.resolve(existingEv, ev);
                    if (resolution != null) {
                        conflictResolutions.add(resolution);
                        if (resolution.getWinningValue().equals(ev.getContentSnippet())) {
                            entityMap.put(key, ev);
                        }
                    }
                } else if (key != null) {
                    entityMap.put(key, ev);
                    finalEvidences.add(ev);
                } else {
                    finalEvidences.add(ev);
                }
            }

            fusedDomainBundle.setEvidences(finalEvidences);
            fusedDomainBundle.recalculateAggregateScore();
            fusedBundles.put(domain, fusedDomainBundle);

            fusionDecisions.add(FusionDecision.builder()
                    .targetDomain(domain)
                    .action("FUSED_DOMAIN_BUNDLE")
                    .details(String.format("Fused domain '%s' into %d evidence items", domain, finalEvidences.size()))
                    .build());
        });

        // Store fused bundles into AtlasContext
        fusedBundles.values().forEach(context::addEvidenceBundle);

        return new ContextFusionResult(fusedBundles, fusionDecisions, conflictResolutions);
    }

    public record ContextFusionResult(
            Map<String, EvidenceBundle> fusedBundles,
            List<FusionDecision> fusionDecisions,
            List<ConflictResolution> conflictResolutions
    ) {}
}
