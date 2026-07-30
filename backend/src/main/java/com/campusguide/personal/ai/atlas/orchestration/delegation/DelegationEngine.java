package com.campusguide.personal.ai.atlas.orchestration.delegation;

import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Engine responsible for evaluating agent suitability and assigning tasks based on capability, load, priority, and locality.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DelegationEngine {

    private final AgentRegistry agentRegistry;

    public Optional<TaskAssignment> delegateTask(String taskId, String requiredCapability, int priority, String localityKey, DelegationPolicy policy) {
        if (taskId == null) {
            log.warn("Cannot delegate task with null taskId");
            return Optional.empty();
        }

        DelegationPolicy effectivePolicy = policy != null ? policy : DelegationPolicy.defaultPolicy();
        List<AgentDescriptor> candidates = agentRegistry.getAllDescriptors().stream()
                .filter(AgentDescriptor::isAvailable)
                .filter(d -> requiredCapability == null || d.supportsCapability(requiredCapability))
                .filter(d -> d.getCurrentLoad() <= effectivePolicy.getMaxLoadThreshold())
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            if (effectivePolicy.isFallbackAllowed()) {
                log.warn("No direct capability match for '{}'. Checking fallback agents.", requiredCapability);
                candidates = agentRegistry.getAllDescriptors().stream()
                        .filter(AgentDescriptor::isAvailable)
                        .filter(d -> d.getCurrentLoad() <= effectivePolicy.getMaxLoadThreshold())
                        .collect(Collectors.toList());
            }

            if (candidates.isEmpty()) {
                log.error("Failed to delegate task {}: No available agents matching policy", taskId);
                return Optional.empty();
            }
        }

        AgentDescriptor selectedAgent = selectBestAgent(candidates, effectivePolicy.getStrategy(), localityKey, priority);
        if (selectedAgent == null) {
            log.error("Failed to delegate task {}: Selection strategy returned null", taskId);
            return Optional.empty();
        }

        String justification = String.format("Assigned agent %s (Type: %s) using %s strategy. Current load: %d/%d.",
                selectedAgent.getAgentId(), selectedAgent.getAgentType(), effectivePolicy.getStrategy(),
                selectedAgent.getCurrentLoad(), selectedAgent.getMaxCapacity());

        TaskAssignment assignment = TaskAssignment.create(taskId, selectedAgent.getAgentId(), priority, localityKey, justification);
        agentRegistry.updateLoad(selectedAgent.getAgentId(), selectedAgent.getCurrentLoad() + 1);

        log.info("DelegationEngine assigned task {} to agent {} (AssignmentId: {})",
                taskId, selectedAgent.getAgentId(), assignment.getAssignmentId());

        return Optional.of(assignment);
    }

    private AgentDescriptor selectBestAgent(List<AgentDescriptor> candidates, AssignmentStrategy strategy, String localityKey, int priority) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        switch (strategy) {
            case LOAD_BALANCED:
                return candidates.stream()
                        .min(Comparator.comparingInt(AgentDescriptor::getCurrentLoad))
                        .orElse(candidates.get(0));

            case PRIORITY_AWARE:
                // For high priority tasks, prefer lowest loaded agent with highest capacity
                return candidates.stream()
                        .min(Comparator.comparingDouble(d -> (double) d.getCurrentLoad() / d.getMaxCapacity()))
                        .orElse(candidates.get(0));

            case LOCALITY_AWARE:
                if (localityKey != null) {
                    Optional<AgentDescriptor> localityMatch = candidates.stream()
                            .filter(d -> d.getMetadata() != null && localityKey.equalsIgnoreCase(d.getMetadata().getDomain()))
                            .min(Comparator.comparingInt(AgentDescriptor::getCurrentLoad));
                    if (localityMatch.isPresent()) {
                        return localityMatch.get();
                    }
                }
                return candidates.stream()
                        .min(Comparator.comparingInt(AgentDescriptor::getCurrentLoad))
                        .orElse(candidates.get(0));

            case CAPABILITY_BASED:
            case HYBRID:
            default:
                return candidates.stream()
                        .min(Comparator.comparingInt(AgentDescriptor::getCurrentLoad)
                                .thenComparing(d -> d.getCapabilities().size(), Comparator.reverseOrder()))
                        .orElse(candidates.get(0));
        }
    }
}
