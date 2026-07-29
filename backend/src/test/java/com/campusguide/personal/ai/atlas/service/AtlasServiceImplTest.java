package com.campusguide.personal.ai.atlas.service;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.dto.AtlasUsageDto;
import com.campusguide.personal.ai.atlas.orchestration.ConversationOrchestrator;
import com.campusguide.platform.user.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtlasServiceImplTest {

    @Mock
    private ConversationOrchestrator conversationOrchestrator;

    @Mock
    private CurrentUserService currentUserService;

    private AtlasServiceImpl atlasService;

    @BeforeEach
    void setUp() {
        atlasService = new AtlasServiceImpl(conversationOrchestrator, currentUserService);
    }

    @Test
    void testChat_DelegatesToOrchestrator() {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("How do I calculate GPA?")
                .build();

        AtlasChatResponse expectedResponse = AtlasChatResponse.builder()
                .id("res-1")
                .conversationId("conv-123")
                .content("GPA is calculated by dividing total quality points by total credit hours.")
                .role("assistant")
                .model("gpt-4o-mini")
                .usage(AtlasUsageDto.builder().promptTokens(10).completionTokens(20).totalTokens(30).build())
                .timestamp(LocalDateTime.now())
                .build();

        when(conversationOrchestrator.orchestrate(request, (String) null)).thenReturn(expectedResponse);

        AtlasChatResponse response = atlasService.chat(request);

        assertNotNull(response);
        assertEquals("res-1", response.getId());
        assertEquals("conv-123", response.getConversationId());
        assertEquals("GPA is calculated by dividing total quality points by total credit hours.", response.getContent());
        verify(conversationOrchestrator).orchestrate(request, (String) null);
    }
}
