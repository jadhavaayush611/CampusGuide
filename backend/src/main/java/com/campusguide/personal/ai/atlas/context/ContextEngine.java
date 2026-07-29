package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.context.metrics.ContextMetrics;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.util.AtlasUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ContextEngine aggregates contextual information across registered ContextContributor implementations
 * and records execution metrics for diagnostics.
 */
@Component
@RequiredArgsConstructor
@lombok.Getter
@Slf4j
public class ContextEngine {

    private final List<ContextContributor> contributors;

    /**
     * Constructs a populated AtlasContext by running all registered ContextContributors.
     *
     * @param request the incoming chat request
     * @return fully enriched AtlasContext with diagnostic metrics
     */
    public AtlasContext buildContext(AtlasChatRequest request) {
        String conversationId = request != null ? request.getConversationId() : null;
        AtlasContext context = new AtlasContext(conversationId, null);
        ContextMetrics metrics = context.getMetrics();
        if (metrics == null) {
            metrics = new ContextMetrics();
            context.setMetrics(metrics);
        }

        if (request == null) {
            return context;
        }

        if (request.getContextPlaceholders() != null) {
            context.putPlaceholders(request.getContextPlaceholders());
        }

        long totalStart = System.currentTimeMillis();

        if (contributors != null) {
            for (ContextContributor contributor : contributors) {
                if (contributor == null) {
                    continue;
                }
                String name = contributor.getName() != null ? contributor.getName() : contributor.getClass().getSimpleName();
                long start = System.currentTimeMillis();

                try {
                    contributor.contribute(request, context);
                    long elapsed = System.currentTimeMillis() - start;
                    metrics.getExecutionTimeMs().put(name, elapsed);
                } catch (Exception e) {
                    long elapsed = System.currentTimeMillis() - start;
                    metrics.getExecutionTimeMs().put(name, elapsed);
                    metrics.getContributorFailures().put(name, e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                    log.warn("Error running ContextContributor [{}]: {}", name, e.getMessage());
                }
            }
        }

        long totalElapsed = System.currentTimeMillis() - totalStart;
        metrics.setTotalExecutionTimeMs(totalElapsed);

        // Calculate estimated context size
        String contextRepresentation = context.getMergedPlaceholders().toString();
        int sizeBytes = contextRepresentation.getBytes(StandardCharsets.UTF_8).length;
        metrics.setEstimatedContextSizeBytes(sizeBytes);
        metrics.setEstimatedTokenCount(AtlasUtils.estimateTokens(contextRepresentation));

        return context;
    }
}
