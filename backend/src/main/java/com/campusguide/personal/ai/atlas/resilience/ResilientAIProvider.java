package com.campusguide.personal.ai.atlas.resilience;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.exception.*;
import com.campusguide.personal.ai.atlas.metrics.AtlasMetrics;
import com.campusguide.personal.ai.atlas.model.AtlasNormalizedResponse;
import com.campusguide.personal.ai.atlas.model.AtlasPrompt;
import com.campusguide.personal.ai.atlas.model.ProviderMetadata;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@Slf4j
public class ResilientAIProvider implements AIProvider {

    private final AIProvider delegate;
    private final AtlasProperties atlasProperties;
    private final CircuitBreaker circuitBreaker;
    private final AtlasMetrics atlasMetrics;

    public ResilientAIProvider(
            @Qualifier("openAIProvider") AIProvider delegate,
            AtlasProperties atlasProperties,
            CircuitBreaker circuitBreaker,
            AtlasMetrics atlasMetrics
    ) {
        this.delegate = delegate;
        this.atlasProperties = atlasProperties;
        this.circuitBreaker = circuitBreaker;
        this.atlasMetrics = atlasMetrics;
    }

    @Override
    public AtlasNormalizedResponse sendPrompt(AtlasPrompt prompt) {
        String providerName = getMetadata().getName();
        String model = (prompt != null && prompt.getModel() != null) ? prompt.getModel() : atlasProperties.getDefaultModel();

        if (!circuitBreaker.allowRequest()) {
            atlasMetrics.recordFailure(providerName, model, AtlasErrorCategory.CIRCUIT_BREAKER_OPEN);
            throw new AtlasProviderUnavailableException(
                    "Circuit breaker is OPEN for provider: " + providerName,
                    AtlasErrorCategory.CIRCUIT_BREAKER_OPEN
            );
        }

        int maxAttempts = atlasProperties.getRetry().isEnabled() ? atlasProperties.getRetry().getMaxAttempts() : 1;
        long intervalMs = atlasProperties.getRetry().getInitialIntervalMs();
        double multiplier = atlasProperties.getRetry().getMultiplier();
        long maxIntervalMs = atlasProperties.getRetry().getMaxIntervalMs();

        Exception lastException = null;
        long startTime = System.currentTimeMillis();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                if (attempt > 1) {
                    atlasMetrics.recordRetry(providerName);
                    log.info("Retrying AI provider request (attempt {}/{})", attempt, maxAttempts);
                }

                AtlasNormalizedResponse response = delegate.sendPrompt(prompt);
                long latency = System.currentTimeMillis() - startTime;

                circuitBreaker.recordSuccess();
                atlasMetrics.recordSuccess(providerName, model);
                atlasMetrics.recordProviderLatency(latency, providerName);

                if (response != null && response.getUsage() != null) {
                    atlasMetrics.recordTokens(
                            response.getUsage().getPromptTokens(),
                            response.getUsage().getCompletionTokens(),
                            response.getUsage().getTotalTokens(),
                            providerName,
                            model
                    );
                }

                return response;
            } catch (Exception e) {
                lastException = e;
                AtlasErrorCategory category = classifyException(e);

                if (category == AtlasErrorCategory.TIMEOUT) {
                    atlasMetrics.recordTimeout(providerName);
                }

                boolean isTransient = isTransientFailure(e, category);

                if (!isTransient || attempt >= maxAttempts) {
                    circuitBreaker.recordFailure();
                    atlasMetrics.recordFailure(providerName, model, category);

                    if (e instanceof AtlasException atlasEx) {
                        throw atlasEx;
                    }
                    throw new AtlasProviderException("AI provider request failed: " + e.getMessage(), e, category);
                }

                log.warn("Transient error on attempt {}/{} for provider {}: {}. Retrying after {} ms",
                        attempt, maxAttempts, providerName, e.getMessage(), intervalMs);

                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AtlasProviderException("Interrupted during retry backoff", ie, AtlasErrorCategory.SYSTEM_ERROR);
                }

                intervalMs = Math.min((long) (intervalMs * multiplier), maxIntervalMs);
            }
        }

        circuitBreaker.recordFailure();
        atlasMetrics.recordFailure(providerName, model, AtlasErrorCategory.PROVIDER_TRANSIENT);
        if (lastException instanceof AtlasException atlasEx) {
            throw atlasEx;
        }
        throw new AtlasProviderException("AI provider request failed after retries", lastException, AtlasErrorCategory.PROVIDER_TRANSIENT);
    }

    @Override
    public ProviderMetadata getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public boolean isAvailable() {
        return delegate.isAvailable() && circuitBreaker.getState() != CircuitBreaker.State.OPEN;
    }

    @Override
    public AtlasNormalizedResponse normalizeResponse(Object rawProviderResponse) {
        return delegate.normalizeResponse(rawProviderResponse);
    }

    private AtlasErrorCategory classifyException(Exception e) {
        if (e instanceof AtlasException atlasEx) {
            return atlasEx.getCategory();
        }
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("timeout") || msg.contains("timed out")) {
            return AtlasErrorCategory.TIMEOUT;
        }
        if (msg.contains("unauthorized") || msg.contains("forbidden") || msg.contains("auth")) {
            return AtlasErrorCategory.AUTHENTICATION;
        }
        if (msg.contains("rate limit") || msg.contains("429") || msg.contains("too many requests")) {
            return AtlasErrorCategory.RATE_LIMIT;
        }
        if (msg.contains("503") || msg.contains("unavailable") || msg.contains("connect")) {
            return AtlasErrorCategory.PROVIDER_TRANSIENT;
        }
        return AtlasErrorCategory.PROVIDER_PERMANENT;
    }

    private boolean isTransientFailure(Exception e, AtlasErrorCategory category) {
        if (category == AtlasErrorCategory.VALIDATION
                || category == AtlasErrorCategory.AUTHENTICATION
                || category == AtlasErrorCategory.PROVIDER_PERMANENT
                || category == AtlasErrorCategory.CIRCUIT_BREAKER_OPEN) {
            return false;
        }
        return category == AtlasErrorCategory.PROVIDER_TRANSIENT
                || category == AtlasErrorCategory.TIMEOUT
                || category == AtlasErrorCategory.RATE_LIMIT;
    }
}
