package com.campusguide.personal.ai.atlas.provider;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.exception.AtlasProviderException;
import com.campusguide.personal.ai.atlas.exception.AtlasProviderUnavailableException;
import com.campusguide.personal.ai.atlas.exception.AtlasTimeoutException;
import com.campusguide.personal.ai.atlas.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Qualifier("groqProvider")
@RequiredArgsConstructor
@Slf4j
public class GroqProvider implements AIProvider {

    private final AtlasProperties atlasProperties;
    private final RestClient groqRestClient;

    @Override
    public AtlasNormalizedResponse sendPrompt(AtlasPrompt prompt) {
        if (!isAvailable()) {
            throw new AtlasProviderUnavailableException("Groq provider is currently disabled or unavailable");
        }

        String apiKey = atlasProperties.getProviders().getGroq().getApiKey();
        String model = (prompt.getModel() != null && !prompt.getModel().isBlank()) 
                ? prompt.getModel() 
                : atlasProperties.getProviders().getGroq().getModel();

        log.info("Sending prompt to Groq Provider. Model: {}, Formatted message count: {}", 
                model, prompt.getFormattedMessages().size());

        // In test or local dev mode without an API key, generate a realistic normalized response
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("mock-") || apiKey.equals("test-key")) {
            return generateMockNormalizedResponse(prompt, model);
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            for (AtlasChatMessage msg : prompt.getFormattedMessages()) {
                Map<String, String> m = new HashMap<>();
                m.put("role", msg.getRole().getValue());
                m.put("content", msg.getContent());
                messages.add(m);
            }
            requestBody.put("messages", messages);

            double temp = prompt.getTemperature() != null ? prompt.getTemperature() : atlasProperties.getProviders().getGroq().getTemperature();
            int maxTok = prompt.getMaxTokens() != null ? prompt.getMaxTokens() : atlasProperties.getProviders().getGroq().getMaxTokens();
            requestBody.put("temperature", temp);
            requestBody.put("max_tokens", maxTok);

            Map<?, ?> rawResponse = groqRestClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (rawResponse == null) {
                throw new AtlasProviderException("Received empty response from Groq provider");
            }

            return normalizeResponse(rawResponse);

        } catch (ResourceAccessException e) {
            log.error("Groq provider timeout or network error: {}", e.getMessage());
            throw new AtlasTimeoutException("Groq request timed out or network was unreachable", e);
        } catch (RestClientResponseException e) {
            log.error("Groq API error with status code {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE || e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new AtlasProviderUnavailableException("Groq service is temporarily unavailable: " + e.getResponseBodyAsString(), e);
            }
            throw new AtlasProviderException("Groq API error: " + e.getResponseBodyAsString(), e);
        } catch (AtlasTimeoutException | AtlasProviderUnavailableException | AtlasProviderException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in Groq provider: {}", e.getMessage(), e);
            throw new AtlasProviderException("Failed to process prompt with Groq provider: " + e.getMessage(), e);
        }
    }

    @Override
    public ProviderMetadata getMetadata() {
        return ProviderMetadata.builder()
                .name("Groq")
                .version("1.0")
                .supportedModels(List.of("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "mixtral-8x7b-32768", "gemma2-9b-it"))
                .active(atlasProperties.isEnabled())
                .build();
    }

    @Override
    public boolean isAvailable() {
        return atlasProperties.isEnabled();
    }

    @Override
    @SuppressWarnings("unchecked")
    public AtlasNormalizedResponse normalizeResponse(Object rawProviderResponse) {
        if (rawProviderResponse instanceof AtlasNormalizedResponse normalized) {
            return normalized;
        }

        if (!(rawProviderResponse instanceof Map<?, ?> rawMap)) {
            throw new AtlasProviderException("Cannot normalize unknown Groq response format: " + rawProviderResponse);
        }

        try {
            Map<String, Object> map = (Map<String, Object>) rawMap;
            String id = map.containsKey("id") ? String.valueOf(map.get("id")) : "groq-" + UUID.randomUUID();
            String model = map.containsKey("model") ? String.valueOf(map.get("model")) : atlasProperties.getProviders().getGroq().getModel();

            List<?> choices = map.get("choices") instanceof List<?> list ? list : Collections.emptyList();
            String content = "";
            String finishReason = "stop";
            AtlasRole role = AtlasRole.ASSISTANT;

            if (!choices.isEmpty() && choices.get(0) instanceof Map<?, ?> rawChoice) {
                Map<String, Object> choice = (Map<String, Object>) rawChoice;
                finishReason = choice.containsKey("finish_reason") ? String.valueOf(choice.get("finish_reason")) : "stop";
                if (choice.get("message") instanceof Map<?, ?> rawMessage) {
                    Map<String, Object> message = (Map<String, Object>) rawMessage;
                    content = message.containsKey("content") ? String.valueOf(message.get("content")) : "";
                    role = AtlasRole.fromValue(message.containsKey("role") ? String.valueOf(message.get("role")) : "assistant");
                }
            }

            int promptTokens = 0;
            int completionTokens = 0;
            int totalTokens = 0;

            if (map.get("usage") instanceof Map<?, ?> rawUsage) {
                Map<String, Object> usage = (Map<String, Object>) rawUsage;
                promptTokens = usage.get("prompt_tokens") instanceof Number n ? n.intValue() : 0;
                completionTokens = usage.get("completion_tokens") instanceof Number n ? n.intValue() : 0;
                totalTokens = usage.get("total_tokens") instanceof Number n ? n.intValue() : 0;
            }

            AtlasUsageInfo usageInfo = AtlasUsageInfo.builder()
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(totalTokens)
                    .build();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("provider", "Groq");
            metadata.put("rawId", id);

            return AtlasNormalizedResponse.builder()
                    .id(id)
                    .content(content)
                    .role(role)
                    .providerName("Groq")
                    .modelUsed(model)
                    .finishReason(finishReason)
                    .usage(usageInfo)
                    .timestamp(LocalDateTime.now())
                    .metadata(metadata)
                    .build();
        } catch (Exception e) {
            log.error("Error normalizing raw Groq response: {}", e.getMessage(), e);
            throw new AtlasProviderException("Failed to normalize Groq response: " + e.getMessage(), e);
        }
    }

    private AtlasNormalizedResponse generateMockNormalizedResponse(AtlasPrompt prompt, String model) {
        String mockContent = "Hello! I am Atlas, your CampusGuide AI advisor (powered by Groq " + model + "). You asked: \"" 
                + (prompt.getUserMessage() != null ? prompt.getUserMessage() : "") 
                + "\". How else can I assist you with your academic goals today?";

        int promptLen = prompt.getUserMessage() != null ? prompt.getUserMessage().length() : 10;
        int promptTokens = Math.max(5, promptLen / 4);
        int completionTokens = Math.max(10, mockContent.length() / 4);

        AtlasUsageInfo usageInfo = AtlasUsageInfo.builder()
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(promptTokens + completionTokens)
                .build();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("provider", "Groq");
        metadata.put("mode", "simulated");

        return AtlasNormalizedResponse.builder()
                .id("atlas-mock-" + UUID.randomUUID())
                .content(mockContent)
                .role(AtlasRole.ASSISTANT)
                .providerName("Groq")
                .modelUsed(model)
                .finishReason("stop")
                .usage(usageInfo)
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
    }
}
