package com.campusguide.personal.ai.atlas.execution.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe registry holding available ToolCapability entries.
 */
@Slf4j
@Component
public class CapabilityRegistry {

    private final Map<String, ToolCapability> capabilities = new ConcurrentHashMap<>();

    public CapabilityRegistry() {
        seedDefaultCapabilities();
    }

    public void registerCapability(ToolCapability capability) {
        if (capability != null && capability.getCapabilityId() != null) {
            capabilities.put(capability.getCapabilityId(), capability);
            log.debug("Registered ToolCapability: id={}, name={}", capability.getCapabilityId(), capability.getCapabilityName());
        }
    }

    public Optional<ToolCapability> getCapability(String capabilityId) {
        return Optional.ofNullable(capabilities.get(capabilityId));
    }

    public boolean hasCapability(String capabilityId) {
        return capabilities.containsKey(capabilityId);
    }

    public List<ToolCapability> getAllCapabilities() {
        return new ArrayList<>(capabilities.values());
    }

    public List<ToolCapability> getCapabilitiesByDomain(String domain) {
        if (domain == null) return new ArrayList<>();
        return capabilities.values().stream()
                .filter(c -> domain.equalsIgnoreCase(c.getDomain()))
                .collect(Collectors.toList());
    }

    private void seedDefaultCapabilities() {
        registerCapability(ToolCapability.builder()
                .capabilityId("cap_academic_query")
                .capabilityName("Academic Query Service")
                .domain("ACADEMIC")
                .toolId("tool_academic")
                .requiredPermissions(List.of("READ_ACADEMICS"))
                .available(true)
                .description("Query student grades, courses, and schedules")
                .build());

        registerCapability(ToolCapability.builder()
                .capabilityId("cap_planner_update")
                .capabilityName("Planner Management Tool")
                .domain("PLANNER")
                .toolId("tool_planner")
                .requiredPermissions(List.of("WRITE_PLANNER"))
                .available(true)
                .description("Create, update, or remove tasks and goals")
                .build());

        registerCapability(ToolCapability.builder()
                .capabilityId("cap_calendar_event")
                .capabilityName("Campus Calendar Service")
                .domain("CALENDAR")
                .toolId("tool_calendar")
                .requiredPermissions(List.of("READ_CALENDAR"))
                .available(true)
                .description("Lookup events, schedules, and deadlines")
                .build());

        registerCapability(ToolCapability.builder()
                .capabilityId("cap_campus_nav")
                .capabilityName("Campus Location & Navigation Service")
                .domain("CAMPUS")
                .toolId("tool_campus")
                .requiredPermissions(List.of("READ_CAMPUS"))
                .available(true)
                .description("Locate buildings, facilities, and campus services")
                .build());
    }
}
