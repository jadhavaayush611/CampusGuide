package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.context.metrics.ContextMetrics;
import com.campusguide.personal.ai.atlas.context.model.UserContext;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextEngineTest {

    @Mock
    private ContextContributor contributor1;

    @Mock
    private ContextContributor contributor2;

    private ContextEngine contextEngine;

    @BeforeEach
    void setUp() {
        contextEngine = new ContextEngine(List.of(contributor1, contributor2));
    }

    @Test
    void testBuildContext_ExecutesAllContributorsAndRecordsMetrics() {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .conversationId("conv-1")
                .contextPlaceholders(Map.of("custom_key", "custom_val"))
                .build();

        when(contributor1.getName()).thenReturn("c1");
        doAnswer(invocation -> {
            AtlasContext ctx = invocation.getArgument(1);
            ctx.setUserContext(UserContext.builder().userId("user-1").name("Alex").build());
            return null;
        }).when(contributor1).contribute(any(), any());

        when(contributor2.getName()).thenReturn("c2");
        doAnswer(invocation -> {
            AtlasContext ctx = invocation.getArgument(1);
            ctx.addContribution("c2", "c2_data");
            return null;
        }).when(contributor2).contribute(any(), any());

        AtlasContext context = contextEngine.buildContext(request);

        assertNotNull(context);
        assertEquals("conv-1", context.getConversationId());
        assertNotNull(context.getUserContext());
        assertEquals("Alex", context.getUserContext().getName());
        assertEquals("c2_data", context.getContribution("c2"));

        ContextMetrics metrics = context.getMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getExecutionTimeMs().containsKey("c1"));
        assertTrue(metrics.getExecutionTimeMs().containsKey("c2"));
        assertTrue(metrics.getEstimatedContextSizeBytes() > 0);
        assertTrue(metrics.getEstimatedTokenCount() > 0);

        verify(contributor1).contribute(eq(request), any(AtlasContext.class));
        verify(contributor2).contribute(eq(request), any(AtlasContext.class));
    }

    @Test
    void testBuildContext_HandlesContributorExceptionGracefullyAndRecordsFailureMetric() {
        AtlasChatRequest request = AtlasChatRequest.builder().build();

        when(contributor1.getName()).thenReturn("failingContributor");
        doThrow(new RuntimeException("Contributor failed")).when(contributor1).contribute(any(), any());

        AtlasContext context = contextEngine.buildContext(request);

        assertNotNull(context);
        ContextMetrics metrics = context.getMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getContributorFailures().containsKey("failingContributor"));
        assertEquals("Contributor failed", metrics.getContributorFailures().get("failingContributor"));

        verify(contributor1).contribute(any(), any());
        verify(contributor2).contribute(any(), any());
    }
}
