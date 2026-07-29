package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.model.AtlasNormalizedResponse;
import com.campusguide.personal.ai.atlas.model.AtlasPrompt;
import com.campusguide.personal.ai.atlas.model.AtlasRole;
import com.campusguide.personal.ai.atlas.model.AtlasUsageInfo;
import com.campusguide.personal.ai.atlas.model.ProviderMetadata;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import com.campusguide.personal.ai.entity.Conversation;
import com.campusguide.personal.ai.entity.Message;
import com.campusguide.personal.ai.enums.MessageRole;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.ai.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ConversationLifecycleIT {

    @Autowired
    private ConversationOrchestrator orchestrator;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @MockitoBean(name = "openAIProvider")
    private AIProvider openAiProvider;

    @BeforeEach
    void setUp() {
        reset(openAiProvider);
        ProviderMetadata metadata = ProviderMetadata.builder().name("MockOpenAI").build();
        when(openAiProvider.getMetadata()).thenReturn(metadata);
        when(openAiProvider.isAvailable()).thenReturn(true);
    }

    @Test
    void testEndToEndConversationLifecycle() {
        AtlasNormalizedResponse mockResponse1 = AtlasNormalizedResponse.builder()
                .id("resp-turn-1")
                .content("Data Structures is recommended for your second year.")
                .role(AtlasRole.ASSISTANT)
                .providerName("MockOpenAI")
                .modelUsed("gpt-4o-mini")
                .usage(new AtlasUsageInfo(12, 18, 30))
                .timestamp(LocalDateTime.now())
                .metadata(Map.of())
                .build();

        when(openAiProvider.sendPrompt(any(AtlasPrompt.class))).thenReturn(mockResponse1);

        AtlasChatRequest request1 = AtlasChatRequest.builder()
                .prompt("What courses should I take?")
                .build();

        AtlasChatResponse response1 = orchestrator.orchestrate(request1, "student-123");

        assertNotNull(response1);
        assertNotNull(response1.getConversationId());
        String generatedConvId = response1.getConversationId();

        Optional<Conversation> savedConvOpt = conversationRepository.findById(generatedConvId);
        assertTrue(savedConvOpt.isPresent());
        assertEquals("student-123", savedConvOpt.get().getUserId());

        List<Message> messagesTurn1 = messageRepository.findByConversationIdOrderByTimestampAsc(generatedConvId);
        assertEquals(2, messagesTurn1.size());
        assertEquals(MessageRole.USER, messagesTurn1.get(0).getRole());
        assertEquals("What courses should I take?", messagesTurn1.get(0).getContent());
        assertEquals(MessageRole.ASSISTANT, messagesTurn1.get(1).getRole());
        assertEquals("Data Structures is recommended for your second year.", messagesTurn1.get(1).getContent());

        AtlasNormalizedResponse mockResponse2 = AtlasNormalizedResponse.builder()
                .id("resp-turn-2")
                .content("Algorithms will follow Data Structures next semester.")
                .role(AtlasRole.ASSISTANT)
                .providerName("MockOpenAI")
                .modelUsed("gpt-4o-mini")
                .usage(new AtlasUsageInfo(25, 20, 45))
                .timestamp(LocalDateTime.now())
                .metadata(Map.of())
                .build();

        when(openAiProvider.sendPrompt(any(AtlasPrompt.class))).thenReturn(mockResponse2);

        AtlasChatRequest request2 = AtlasChatRequest.builder()
                .conversationId(generatedConvId)
                .prompt("What comes after Data Structures?")
                .build();

        AtlasChatResponse response2 = orchestrator.orchestrate(request2, "student-123");

        assertNotNull(response2);
        assertEquals(generatedConvId, response2.getConversationId());

        List<Message> messagesTurn2 = messageRepository.findByConversationIdOrderByTimestampAsc(generatedConvId);
        assertEquals(4, messagesTurn2.size());
        assertEquals("What comes after Data Structures?", messagesTurn2.get(2).getContent());
        assertEquals("Algorithms will follow Data Structures next semester.", messagesTurn2.get(3).getContent());
    }
}
