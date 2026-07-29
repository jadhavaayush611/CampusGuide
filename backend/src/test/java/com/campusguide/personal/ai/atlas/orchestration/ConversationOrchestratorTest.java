package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.ContextEngine;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.mapper.AtlasMapper;
import com.campusguide.personal.ai.atlas.model.*;
import com.campusguide.personal.ai.atlas.prompt.PromptBuilder;
import com.campusguide.personal.ai.atlas.prompt.ContextSectionAssembler;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import com.campusguide.personal.ai.atlas.validation.AtlasPromptValidator;
import com.campusguide.personal.ai.entity.Conversation;
import com.campusguide.personal.ai.entity.Message;
import com.campusguide.personal.ai.enums.MessageRole;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.ai.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationOrchestratorTest {

    @Mock
    private AIProvider aiProvider;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private ContextSectionAssembler contextSectionAssembler;

    @Mock
    private AtlasPromptValidator atlasPromptValidator;

    @Mock
    private ContextEngine contextEngine;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    private AtlasMapper atlasMapper;
    private ConversationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        atlasMapper = new AtlasMapper();
        orchestrator = new ConversationOrchestrator(
                aiProvider,
                promptBuilder,
                contextSectionAssembler,
                atlasPromptValidator,
                atlasMapper,
                contextEngine,
                conversationRepository,
                messageRepository
        );
    }

    @Test
    void testOrchestrate_Success_WithNewConversation() {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("What is computer science?")
                .build();

        AtlasContext context = new AtlasContext(null, null);
        context.putPlaceholder("student_name", "Student");

        AtlasPrompt prompt = AtlasPrompt.builder()
                .userMessage("What is computer science?")
                .systemPrompt("Default prompt")
                .formattedMessages(List.of())
                .build();

        AtlasNormalizedResponse normalizedResponse = AtlasNormalizedResponse.builder()
                .id("resp-101")
                .content("Computer science is the study of computation and information.")
                .role(AtlasRole.ASSISTANT)
                .providerName("OpenAI")
                .modelUsed("gpt-4o-mini")
                .finishReason("stop")
                .usage(new AtlasUsageInfo(15, 25, 40))
                .timestamp(LocalDateTime.now())
                .metadata(Map.of())
                .build();

        when(contextEngine.buildContext(any())).thenReturn(context);
        when(contextSectionAssembler.assembleSections(context)).thenReturn(List.of());
        when(promptBuilder.buildPrompt(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(prompt);
        when(aiProvider.sendPrompt(prompt)).thenReturn(normalizedResponse);
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AtlasChatResponse response = orchestrator.orchestrate(request);

        assertNotNull(response);
        assertNotNull(response.getConversationId());
        assertEquals("resp-101", response.getId());
        assertEquals("Computer science is the study of computation and information.", response.getContent());
        assertEquals(40, response.getUsage().getTotalTokens());

        verify(atlasPromptValidator).validateRequest(request);
        verify(conversationRepository, times(1)).save(any(Conversation.class));
        verify(messageRepository, times(2)).save(any(Message.class)); // 1 User, 1 Assistant
        verify(aiProvider).sendPrompt(prompt);
    }

    @Test
    void testOrchestrate_ExistingConversation_LoadsHistory() {
        String existingConvId = "conv-existing-999";
        AtlasChatRequest request = AtlasChatRequest.builder()
                .conversationId(existingConvId)
                .prompt("Tell me more about AI.")
                .build();

        Conversation existingConv = Conversation.builder()
                .id(existingConvId)
                .userId("student-1")
                .build();

        Message historicalMsg1 = Message.builder()
                .conversationId(existingConvId)
                .role(MessageRole.USER)
                .content("Hi")
                .timestamp(Instant.now().minusSeconds(60))
                .build();

        Message historicalMsg2 = Message.builder()
                .conversationId(existingConvId)
                .role(MessageRole.ASSISTANT)
                .content("Hello! How can I help you?")
                .timestamp(Instant.now().minusSeconds(50))
                .build();

        AtlasContext context = new AtlasContext(existingConvId, "student-1");
        AtlasPrompt prompt = AtlasPrompt.builder().userMessage("Tell me more about AI.").build();
        AtlasNormalizedResponse normalizedResponse = AtlasNormalizedResponse.builder()
                .id("resp-202")
                .content("AI encompasses machine learning, NLP, and robotics.")
                .role(AtlasRole.ASSISTANT)
                .providerName("OpenAI")
                .modelUsed("gpt-4o-mini")
                .usage(new AtlasUsageInfo(20, 30, 50))
                .build();

        when(conversationRepository.findById(existingConvId)).thenReturn(Optional.of(existingConv));
        when(messageRepository.findByConversationIdOrderByTimestampAsc(existingConvId)).thenReturn(List.of(historicalMsg1, historicalMsg2));
        when(contextEngine.buildContext(request)).thenReturn(context);
        when(contextSectionAssembler.assembleSections(context)).thenReturn(List.of());
        when(promptBuilder.buildPrompt(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(prompt);
        when(aiProvider.sendPrompt(prompt)).thenReturn(normalizedResponse);

        AtlasChatResponse response = orchestrator.orchestrate(request);

        assertNotNull(response);
        assertEquals(existingConvId, response.getConversationId());
        assertEquals("AI encompasses machine learning, NLP, and robotics.", response.getContent());

        // Verify history was loaded and passed to prompt builder
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AtlasChatMessage>> historyCaptor = ArgumentCaptor.forClass(List.class);
        verify(promptBuilder).buildPrompt(eq("Tell me more about AI."), any(), historyCaptor.capture(), any(), any(), any(), any(), any());

        List<AtlasChatMessage> loadedHistory = historyCaptor.getValue();
        assertEquals(2, loadedHistory.size());
        assertEquals(AtlasRole.USER, loadedHistory.get(0).getRole());
        assertEquals("Hi", loadedHistory.get(0).getContent());
        assertEquals(AtlasRole.ASSISTANT, loadedHistory.get(1).getRole());
        assertEquals("Hello! How can I help you?", loadedHistory.get(1).getContent());
    }
}
