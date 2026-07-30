package com.campusguide.personal.ai.atlas.execution.tool;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ToolResolverTest {

    private ToolResolver resolver;
    private CapabilityRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CapabilityRegistry();
        resolver = new ToolResolver(registry);
    }

    @Test
    @DisplayName("ToolResolver resolves registered capability")
    void testResolveRegisteredCapability() {
        ExecutionUnit unit = ExecutionUnit.builder()
                .unitId("u_1")
                .targetCapability("cap_academic_query")
                .unitType(ExecutionUnitType.QUERY)
                .build();

        ExecutionStage stage = ExecutionStage.builder()
                .stageId("s_1")
                .executionUnits(List.of(unit))
                .build();

        ExecutionContext ctx = ExecutionContext.builder().build();

        ToolResolver.ToolResolutionResult result = resolver.resolve(ctx, List.of(stage));

        assertNotNull(result);
        assertTrue(result.isAllCapabilitiesResolved());
        assertEquals(1, result.getResolvedCapabilities().size());
        assertEquals("cap_academic_query", result.getResolvedCapabilities().get(0).getCapabilityId());
    }

    @Test
    @DisplayName("ToolResolver detects missing capability and recommends alternatives")
    void testResolveMissingCapability() {
        ExecutionUnit unit = ExecutionUnit.builder()
                .unitId("u_2")
                .targetCapability("cap_unknown_service")
                .unitType(ExecutionUnitType.ACTION)
                .build();

        ExecutionStage stage = ExecutionStage.builder()
                .stageId("s_2")
                .executionUnits(List.of(unit))
                .build();

        ExecutionContext ctx = ExecutionContext.builder().build();

        ToolResolver.ToolResolutionResult result = resolver.resolve(ctx, List.of(stage));

        assertNotNull(result);
        assertFalse(result.isAllCapabilitiesResolved());
        assertTrue(result.getMissingCapabilities().contains("cap_unknown_service"));
        assertNotNull(result.getAlternativeRecommendations().get("cap_unknown_service"));
    }
}
