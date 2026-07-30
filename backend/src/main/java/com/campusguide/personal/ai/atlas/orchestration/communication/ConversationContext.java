package com.campusguide.personal.ai.atlas.orchestration.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Context holder for multi-agent conversation sessions and negotiations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContext {

    private String conversationId;
    private String initiatorAgentId;
    @Builder.Default
    private List<String> participantAgentIds = new ArrayList<>();
    @Builder.Default
    private List<AgentMessage> messages = new ArrayList<>();
    @Builder.Default
    private Instant createdAt = Instant.now();
    @Builder.Default
    private ConversationStatus status = ConversationStatus.ACTIVE;
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    public enum ConversationStatus {
        ACTIVE,
        COMPLETED,
        CANCELLED,
        FAILED
    }

    public static ConversationContext start(String initiatorId, List<String> participants) {
        List<String> allParticipants = new ArrayList<>();
        if (initiatorId != null) allParticipants.add(initiatorId);
        if (participants != null) {
            for (String p : participants) {
                if (!allParticipants.contains(p)) {
                    allParticipants.add(p);
                }
            }
        }
        return ConversationContext.builder()
                .conversationId("conv_" + UUID.randomUUID().toString().substring(0, 8))
                .initiatorAgentId(initiatorId)
                .participantAgentIds(allParticipants)
                .createdAt(Instant.now())
                .status(ConversationStatus.ACTIVE)
                .build();
    }

    public void addMessage(AgentMessage message) {
        if (message != null) {
            this.messages.add(message);
        }
    }
}
