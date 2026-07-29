package com.campusguide.personal.ai.atlas.context.retrieval;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.metrics.ContextMetrics;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Executes selective retrieval strategies based on QueryContext and RetrievalPolicy.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContextRetriever {

    private final List<RetrievalStrategy> strategies;
    private final RetrievalPolicy retrievalPolicy;

    public RetrievalContext retrieveContext(AtlasChatRequest request, QueryContext queryContext, AtlasContext atlasContext) {
        RetrievalPolicy policy = retrievalPolicy != null ? retrievalPolicy : new RetrievalPolicy();

        RetrievalContext retrievalContext = RetrievalContext.builder()
                .request(request)
                .queryContext(queryContext)
                .policy(policy)
                .startTimeMs(System.currentTimeMillis())
                .build();

        ContextMetrics metrics = atlasContext != null ? atlasContext.getMetrics() : null;

        if (strategies == null || strategies.isEmpty()) {
            retrievalContext.setTotalLatencyMs(System.currentTimeMillis() - retrievalContext.getStartTimeMs());
            return retrievalContext;
        }

        for (RetrievalStrategy strategy : strategies) {
            String name = strategy.getStrategyName();
            long strategyStart = System.currentTimeMillis();

            try {
                if (strategy.supports(queryContext, policy)) {
                    log.debug("Executing retrieval strategy [{}] for query intent [{}]", name, queryContext != null ? queryContext.getIntent() : "null");
                    strategy.retrieve(retrievalContext, atlasContext);

                    long elapsed = System.currentTimeMillis() - strategyStart;
                    if (metrics != null) {
                        metrics.getExecutionTimeMs().put(name, elapsed);
                    }

                    retrievalContext.getExecutedStrategies().add(name);
                } else {
                    log.debug("Skipping retrieval strategy [{}] for query intent [{}]", name, queryContext != null ? queryContext.getIntent() : "null");
                    retrievalContext.getSkippedStrategies().add(name);
                }
            } catch (Exception e) {
                long elapsed = System.currentTimeMillis() - strategyStart;
                if (metrics != null) {
                    metrics.getExecutionTimeMs().put(name, elapsed);
                    metrics.getContributorFailures().put(name, e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                }
                log.warn("Error executing retrieval strategy [{}]: {}", name, e.getMessage());
                retrievalContext.getSkippedStrategies().add(name);
            }
        }

        retrievalContext.setTotalLatencyMs(System.currentTimeMillis() - retrievalContext.getStartTimeMs());
        return retrievalContext;
    }
}
