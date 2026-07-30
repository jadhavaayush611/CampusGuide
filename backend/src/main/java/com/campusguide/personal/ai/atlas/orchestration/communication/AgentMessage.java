package com.campusguide.personal.ai.atlas.orchestration.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Message model for inter-agent communication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentMessage {

    private String messageId;
    private String senderAgentId;
    private String recipientAgentId; // Null or '*' for broadcast
    private String conversationId;
    private MessageType messageType;
    @Builder.Default
    private Instant timestamp = Instant.now();
    @Builder.Default
    private int priority = 5;
    private String correlationId;

    @Builder.Default
    private Map<String, Object> payload = new HashMap<>();

    public enum MessageType {
        REQUEST,
        RESPONSE,
        BROADCAST,
        NEGOTIATION,
        COORDINATION,
        CANCELLATION,
        HEARTBEAT
    }

    public static AgentMessage request(String sender, String recipient, String conversationId, Map<String, Object> payload) {
        return AgentMessage.builder()
                .messageId("msg_" + UUID.randomUUID().toString().substring(0, 8))
                .senderAgentId(sender)
                .recipientAgentId(recipient)
                .conversationId(conversationId)
                .messageType(MessageType.REQUEST)
                .payload(payload)
                .timestamp(Instant.now())
                .build();
    }

    public static AgentMessage response(String sender, String recipient, String conversationId, String correlationId, Map<String, Object> payload) {
        return AgentMessage.builder()
                .messageId("msg_" + UUID.randomUUID().toString().substring(0, 8))
                .senderAgentId(sender)
                .recipientAgentId(recipient)
                .conversationId(conversationId)
                .correlationId(correlationId)
                .messageType(MessageType.RESPONSE)
                .payload(payload)
                .timestamp(Instant.now())
                .build();
    }

    public static AgentMessage broadcast(String sender, String conversationId, Map<String, Object> payload) {
        return AgentMessage.builder()
                .messageId("msg_" + UUID.randomUUID().toString().substring(0, 8))
                .senderAgentId(sender)
                .recipientAgentId("*")
                .conversationId(conversationId)
                .messageType(MessageType.BROADCAST)
                .payload(payload)
                .timestamp(Instant.now())
                .build();
    }
}
