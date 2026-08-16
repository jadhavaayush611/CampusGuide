package com.campusguide.personal.ai.atlas.provider;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.exception.*;
import com.campusguide.personal.ai.atlas.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GroqProviderTest {

    private RestClient restClient;
    private MockRestServiceServer mockServer;
    private AtlasProperties properties;
    private GroqProvider provider;

    @BeforeEach
    void setUp() {
        properties = new AtlasProperties();
        properties.setDefaultProvider("groq");
        
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer real-test-key")
                .defaultHeader("Content-Type", "application/json");
        
        mockServer = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        provider = new GroqProvider(properties, restClient);
    }

    @Test
    void testInstantiationAndGetMetadata() {
        assertNotNull(provider);
        ProviderMetadata metadata = provider.getMetadata();
        assertNotNull(metadata);
        assertEquals("Groq", metadata.getName());
        assertEquals("1.0", metadata.getVersion());
        assertTrue(metadata.getSupportedModels().contains("llama-3.3-70b-versatile"));
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
        properties.getProviders().getGroq().setApiKey("mock-key");
        AtlasPrompt prompt = AtlasPrompt.builder()
                .userMessage("Tell me about course registration")
                .formattedMessages(List.of(AtlasChatMessage.builder().role(AtlasRole.USER).content("Tell me about course registration").build()))
                .build();

        AtlasNormalizedResponse response = provider.sendPrompt(prompt);

        assertNotNull(response);
        assertEquals("Groq", response.getProviderName());
        assertEquals(AtlasRole.ASSISTANT, response.getRole());
        assertTrue(response.getContent().contains("CampusGuide AI advisor"));
    }

    @Test
    void testSendPrompt_Success_WithRealClientCall() {
        properties.getProviders().getGroq().setApiKey("real-test-key");

        mockServer.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer real-test-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(
                        "{\"id\":\"groq-123\",\"model\":\"llama-3.3-70b-versatile\"," +
                        "\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"role\":\"assistant\",\"content\":\"Hello from Groq API!\"}}]," +
                        "\"usage\":{\"prompt_tokens\":10,\"completion_tokens\":20,\"total_tokens\":30}}",
                        MediaType.APPLICATION_JSON));

        AtlasPrompt prompt = AtlasPrompt.builder()
                .userMessage("Hello")
                .formattedMessages(List.of(AtlasChatMessage.builder().role(AtlasRole.USER).content("Hello").build()))
                .build();

        AtlasNormalizedResponse response = provider.sendPrompt(prompt);
        assertNotNull(response);
        assertEquals("groq-123", response.getId());
        assertEquals("Hello from Groq API!", response.getContent());
        assertEquals("Groq", response.getProviderName());
        assertEquals(30, response.getUsage().getTotalTokens());
        mockServer.verify();
    }

    @Test
    void testSendPrompt_Http401_ThrowsProviderException() {
        properties.getProviders().getGroq().setApiKey("invalid-key");

        mockServer.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid API Key\"}"));

        AtlasPrompt prompt = AtlasPrompt.builder()
                .userMessage("Hello")
                .formattedMessages(List.of(AtlasChatMessage.builder().role(AtlasRole.USER).content("Hello").build()))
                .build();

        assertThrows(AtlasProviderException.class, () -> provider.sendPrompt(prompt));
        mockServer.verify();
    }

    @Test
    void testSendPrompt_Http429_ThrowsProviderUnavailableException() {
        properties.getProviders().getGroq().setApiKey("real-test-key");

        mockServer.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("{\"error\":\"Rate limit exceeded\"}"));

        AtlasPrompt prompt = AtlasPrompt.builder()
                .userMessage("Hello")
                .formattedMessages(List.of(AtlasChatMessage.builder().role(AtlasRole.USER).content("Hello").build()))
                .build();

        assertThrows(AtlasProviderUnavailableException.class, () -> provider.sendPrompt(prompt));
        mockServer.verify();
    }

    @Test
    void testSendPrompt_Http5xx_ThrowsProviderUnavailableException() {
        properties.getProviders().getGroq().setApiKey("real-test-key");

        mockServer.expect(requestTo("https://api.groq.com/openai/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE).body("{\"error\":\"Server Overloaded\"}"));

        AtlasPrompt prompt = AtlasPrompt.builder()
                .userMessage("Hello")
                .formattedMessages(List.of(AtlasChatMessage.builder().role(AtlasRole.USER).content("Hello").build()))
                .build();

        assertThrows(AtlasProviderUnavailableException.class, () -> provider.sendPrompt(prompt));
        mockServer.verify();
    }

    @Test
    void testNormalizeResponse_ValidMap_NormalizesCorrectly() {
        Map<String, Object> rawResponse = new HashMap<>();
        rawResponse.put("id", "groq-chat-123");
        rawResponse.put("model", "llama-3.3-70b-versatile");

        Map<String, Object> choice = new HashMap<>();
        choice.put("finish_reason", "stop");
        Map<String, String> message = new HashMap<>();
        message.put("role", "assistant");
        message.put("content", "Normalized content response from Groq");
        choice.put("message", message);

        rawResponse.put("choices", List.of(choice));

        Map<String, Integer> usage = new HashMap<>();
        usage.put("prompt_tokens", 15);
        usage.put("completion_tokens", 25);
        usage.put("total_tokens", 40);
        rawResponse.put("usage", usage);

        AtlasNormalizedResponse normalized = provider.normalizeResponse(rawResponse);

        assertNotNull(normalized);
        assertEquals("groq-chat-123", normalized.getId());
        assertEquals("llama-3.3-70b-versatile", normalized.getModelUsed());
        assertEquals("Normalized content response from Groq", normalized.getContent());
        assertEquals(AtlasRole.ASSISTANT, normalized.getRole());
        assertEquals(15, normalized.getUsage().getPromptTokens());
        assertEquals(25, normalized.getUsage().getCompletionTokens());
        assertEquals(40, normalized.getUsage().getTotalTokens());
    }

    @Test
    void testNormalizeResponse_InvalidFormat_ThrowsException() {
        assertThrows(AtlasProviderException.class, () -> provider.normalizeResponse("Invalid String Input"));
    }

    @Test
    void testValidate_MissingApiKey_ThrowsConfigurationException() {
        properties.getProviders().getGroq().setApiKey(null);
        assertThrows(AtlasConfigurationException.class, () -> properties.validate());
    }
}
