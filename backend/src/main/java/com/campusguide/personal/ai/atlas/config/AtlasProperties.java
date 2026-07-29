package com.campusguide.personal.ai.atlas.config;

import com.campusguide.personal.ai.atlas.exception.AtlasConfigurationException;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "atlas")
public class AtlasProperties {

    private boolean enabled = true;
    private String defaultProvider = "openai";
    private String defaultModel = "gpt-4o-mini";
    private String defaultSystemPrompt = "You are Atlas, an intelligent, empathetic, and helpful academic AI advisor for CampusGuide. Provide clear, accurate, and structured advice for university students.";
    private int maxPromptLength = 4096;
    private int promptTokenBudgetCap = 4096;
    private long timeoutMs = 30000;

    private ProvidersProperties providers = new ProvidersProperties();
    private RetryProperties retry = new RetryProperties();
    private CircuitBreakerProperties circuitBreaker = new CircuitBreakerProperties();
    private RateLimitProperties rateLimit = new RateLimitProperties();

    @PostConstruct
    public void validate() {
        if (!enabled) {
            return;
        }

        if (defaultProvider == null || defaultProvider.isBlank()) {
            throw new AtlasConfigurationException("Atlas defaultProvider must not be null or blank");
        }

        if (defaultModel == null || defaultModel.isBlank()) {
            throw new AtlasConfigurationException("Atlas defaultModel must not be null or blank");
        }

        if (maxPromptLength <= 0) {
            throw new AtlasConfigurationException("Atlas maxPromptLength must be greater than 0");
        }

        if (promptTokenBudgetCap <= 0) {
            throw new AtlasConfigurationException("Atlas promptTokenBudgetCap must be greater than 0");
        }

        if (timeoutMs <= 0) {
            throw new AtlasConfigurationException("Atlas timeoutMs must be greater than 0");
        }

        if (providers == null) {
            throw new AtlasConfigurationException("Atlas providers configuration must not be null");
        }

        if ("openai".equalsIgnoreCase(defaultProvider)) {
            OpenAIProperties openai = providers.getOpenai();
            if (openai == null) {
                throw new AtlasConfigurationException("OpenAI provider properties must not be null");
            }
            if (openai.getBaseUrl() == null || openai.getBaseUrl().isBlank()) {
                throw new AtlasConfigurationException("OpenAI baseUrl must not be null or blank");
            }
            if (openai.getModel() == null || openai.getModel().isBlank()) {
                throw new AtlasConfigurationException("OpenAI model must not be null or blank");
            }
            if (openai.getApiKey() == null) {
                throw new AtlasConfigurationException("OpenAI apiKey must not be null");
            }
        }

        if (retry != null) {
            if (retry.getMaxAttempts() < 1) {
                throw new AtlasConfigurationException("Atlas retry maxAttempts must be at least 1");
            }
            if (retry.getInitialIntervalMs() <= 0) {
                throw new AtlasConfigurationException("Atlas retry initialIntervalMs must be greater than 0");
            }
            if (retry.getMultiplier() < 1.0) {
                throw new AtlasConfigurationException("Atlas retry multiplier must be at least 1.0");
            }
            if (retry.getMaxIntervalMs() < retry.getInitialIntervalMs()) {
                throw new AtlasConfigurationException("Atlas retry maxIntervalMs must be greater than or equal to initialIntervalMs");
            }
        }

        if (circuitBreaker != null) {
            if (circuitBreaker.getFailureThreshold() <= 0) {
                throw new AtlasConfigurationException("Circuit breaker failureThreshold must be greater than 0");
            }
            if (circuitBreaker.getWaitDurationInOpenStateMs() <= 0) {
                throw new AtlasConfigurationException("Circuit breaker waitDurationInOpenStateMs must be greater than 0");
            }
            if (circuitBreaker.getPermittedNumberOfCallsInHalfOpenState() <= 0) {
                throw new AtlasConfigurationException("Circuit breaker permittedNumberOfCallsInHalfOpenState must be greater than 0");
            }
        }

        if (rateLimit != null) {
            if (rateLimit.getRequestsPerMinute() <= 0) {
                throw new AtlasConfigurationException("Rate limit requestsPerMinute must be greater than 0");
            }
            if (rateLimit.getCapacity() <= 0) {
                throw new AtlasConfigurationException("Rate limit capacity must be greater than 0");
            }
        }
    }

    @Data
    public static class ProvidersProperties {
        private OpenAIProperties openai = new OpenAIProperties();
    }

    @Data
    public static class OpenAIProperties {
        private String apiKey = "";
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-4o-mini";
        private double temperature = 0.7;
        private int maxTokens = 1024;
    }

    @Data
    public static class RetryProperties {
        private boolean enabled = true;
        private int maxAttempts = 3;
        private long initialIntervalMs = 500;
        private double multiplier = 2.0;
        private long maxIntervalMs = 5000;
    }

    @Data
    public static class CircuitBreakerProperties {
        private boolean enabled = true;
        private int failureThreshold = 5;
        private long waitDurationInOpenStateMs = 30000;
        private int permittedNumberOfCallsInHalfOpenState = 3;
    }

    @Data
    public static class RateLimitProperties {
        private boolean enabled = true;
        private int requestsPerMinute = 60;
        private int capacity = 60;
    }
}
