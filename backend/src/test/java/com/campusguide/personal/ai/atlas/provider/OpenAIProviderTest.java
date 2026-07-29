package com.campusguide.personal.ai.atlas.provider;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.exception.AtlasProviderException;
import com.campusguide.personal.ai.atlas.exception.AtlasProviderUnavailableException;
import com.campusguide.personal.ai.atlas.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenAIProviderTest {

    @Mock
    private RestClient restClient;

    private AtlasProperties properties;
    private OpenAIProvider provider;

    @BeforeEach
    void setUp() {
        properties = new AtlasProperties();
        properties.getProviders().getOpenai().setApiKey("mock-key");
        provider = new OpenAIProvider(properties, restClient);
    }

    @Test
    void testGetMetadata() {
        ProviderMetadata metadata = provider.getMetadata();
        assertNotNull(metadata);
        assertEquals("OpenAI", metadata.getName());
        assertEquals("1.0", metadata.getVersion());
        assertTrue(metadata.getSupportedModels().contains("gpt-4o-mini"));
        assertTrue(metadata.isActive());
    }

    @Test
    void testIsAvailable() {
        assertTrue(provider.isAvailable());
        properties.setEnabled(false);
        assertFalse(provider.isAvailable());
    }

    @Test
    void testSendPrompt_DisabledProvider_ThrowsException() {
        properties.setEnabled(false);
        AtlasPrompt prompt = AtlasPrompt.builder().userMessage("Hello").build();
        assertThrows(AtlasProviderUnavailableException.class, () -> provider.sendPrompt(prompt));
    }

    @Test
    void testSendPrompt_MockApiKey_ReturnsNormalizedResponse() {
        AtlasPrompt prompt = AtlasPrompt.builder()
                .userMessage("Tell me about course registration")
                .formattedMessages(List.of(AtlasChatMessage.builder().role(AtlasRole.USER).content("Tell me about course registration").build()))
                .build();

        AtlasNormalizedResponse response = provider.sendPrompt(prompt);

        assertNotNull(response);
        assertEquals("OpenAI", response.getProviderName());
        assertEquals(AtlasRole.ASSISTANT, response.getRole());
        assertTrue(response.getContent().contains("CampusGuide AI advisor"));
        assertNotNull(response.getUsage());
        assertTrue(response.getUsage().getTotalTokens() > 0);
    }

    @Test
    void testNormalizeResponse_ValidMap_NormalizesCorrectly() {
        Map<String, Object> rawResponse = new HashMap<>();
        rawResponse.put("id", "chatcmpl-123");
        rawResponse.put("model", "gpt-4o-mini");

        Map<String, Object> choice = new HashMap<>();
        choice.put("finish_reason", "stop");
        Map<String, String> message = new HashMap<>();
        message.put("role", "assistant");
        message.put("content", "Normalized content response");
        choice.put("message", message);

        rawResponse.put("choices", List.of(choice));

        Map<String, Integer> usage = new HashMap<>();
        usage.put("prompt_tokens", 15);
        usage.put("completion_tokens", 25);
        usage.put("total_tokens", 40);
        rawResponse.put("usage", usage);

        AtlasNormalizedResponse normalized = provider.normalizeResponse(rawResponse);

        assertNotNull(normalized);
        assertEquals("chatcmpl-123", normalized.getId());
        assertEquals("gpt-4o-mini", normalized.getModelUsed());
        assertEquals("Normalized content response", normalized.getContent());
        assertEquals(AtlasRole.ASSISTANT, normalized.getRole());
        assertEquals(15, normalized.getUsage().getPromptTokens());
        assertEquals(25, normalized.getUsage().getCompletionTokens());
        assertEquals(40, normalized.getUsage().getTotalTokens());
    }

    @Test
    void testNormalizeResponse_InvalidFormat_ThrowsException() {
        assertThrows(AtlasProviderException.class, () -> provider.normalizeResponse("Invalid String Input"));
    }
}
