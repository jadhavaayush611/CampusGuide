package com.campusguide.modules.ai.prompt;

import com.campusguide.modules.ai.dto.gateway.AiGatewayRequest;
import com.campusguide.modules.ai.dto.gateway.GatewayMessage;
import com.campusguide.modules.ai.enums.ConversationType;
import com.campusguide.modules.ai.exception.PromptBuildException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderTest {

    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder();
        // Initialize with system prompts since in unit tests Spring won't load resources automatically
        promptBuilder.setSystemPrompt(ConversationType.GENERAL_CHAT, "You are a helpful, friendly campus assistant.");
        promptBuilder.setSystemPrompt(ConversationType.ACADEMIC_ADVISOR, "You are an academic advisor.");
        promptBuilder.setSystemPrompt(ConversationType.CAREER_GUIDANCE, "You are a career guidance counselor.");
        promptBuilder.setSystemPrompt(ConversationType.CAMPUS_ASSISTANT, "You are a campus guide.");
    }

    @Test
    void buildPayload_GeneralChat_Success() {
        List<GatewayMessage> history = new ArrayList<>();
        AiGatewayRequest request = promptBuilder.buildPayload(
                "corr-123",
                "conv-123",
                ConversationType.GENERAL_CHAT,
                "Hello",
                history,
                new HashMap<>()
        );

        assertNotNull(request);
        assertEquals("corr-123", request.getCorrelationId());
        assertEquals("conv-123", request.getConversationId());
        assertEquals("GENERAL_CHAT", request.getConversationType());
        assertEquals("Hello", request.getUserMessage());
        assertEquals("You are a helpful, friendly campus assistant.", request.getMetadata().get("systemPrompt"));
    }

    @Test
    void buildPayload_AcademicAdvisor_Success() {
        List<GatewayMessage> history = new ArrayList<>();
        AiGatewayRequest request = promptBuilder.buildPayload(
                "corr-123",
                "conv-123",
                ConversationType.ACADEMIC_ADVISOR,
                "Need course advice",
                history,
                new HashMap<>()
        );

        assertNotNull(request);
        assertEquals("corr-123", request.getCorrelationId());
        assertEquals("ACADEMIC_ADVISOR", request.getConversationType());
        assertEquals("You are an academic advisor.", request.getMetadata().get("systemPrompt"));
    }

    @Test
    void buildPayload_CareerGuidance_Success() {
        List<GatewayMessage> history = new ArrayList<>();
        AiGatewayRequest request = promptBuilder.buildPayload(
                "corr-123",
                "conv-123",
                ConversationType.CAREER_GUIDANCE,
                "Need resume help",
                history,
                new HashMap<>()
        );

        assertNotNull(request);
        assertEquals("corr-123", request.getCorrelationId());
        assertEquals("CAREER_GUIDANCE", request.getConversationType());
        assertEquals("You are a career guidance counselor.", request.getMetadata().get("systemPrompt"));
    }

    @Test
    void buildPayload_CampusAssistant_Success() {
        List<GatewayMessage> history = new ArrayList<>();
        AiGatewayRequest request = promptBuilder.buildPayload(
                "corr-123",
                "conv-123",
                ConversationType.CAMPUS_ASSISTANT,
                "Where is the library?",
                history,
                new HashMap<>()
        );

        assertNotNull(request);
        assertEquals("corr-123", request.getCorrelationId());
        assertEquals("CAMPUS_ASSISTANT", request.getConversationType());
        assertEquals("You are a campus guide.", request.getMetadata().get("systemPrompt"));
    }

    @Test
    void buildPayload_NullInputs_ThrowsPromptBuildException() {
        assertThrows(PromptBuildException.class, () -> 
                promptBuilder.buildPayload(null, "conv-123", ConversationType.GENERAL_CHAT, "Hello", new ArrayList<>(), new HashMap<>()));

        assertThrows(PromptBuildException.class, () -> 
                promptBuilder.buildPayload("corr-123", null, ConversationType.GENERAL_CHAT, "Hello", new ArrayList<>(), new HashMap<>()));

        assertThrows(PromptBuildException.class, () -> 
                promptBuilder.buildPayload("corr-123", "conv-123", null, "Hello", new ArrayList<>(), new HashMap<>()));

        assertThrows(PromptBuildException.class, () -> 
                promptBuilder.buildPayload("corr-123", "conv-123", ConversationType.GENERAL_CHAT, "", new ArrayList<>(), new HashMap<>()));
    }
}
