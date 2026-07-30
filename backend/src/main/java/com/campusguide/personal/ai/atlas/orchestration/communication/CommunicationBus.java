package com.campusguide.personal.ai.atlas.orchestration.communication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory CommunicationBus facilitating asynchronous request/response, broadcasts, negotiation, coordination, and cancellation.
 */
@Slf4j
@Component
public class CommunicationBus {

    private final Map<String, List<AgentMessage>> agentInboxes = new ConcurrentHashMap<>();
    private final Map<String, ConversationContext> activeConversations = new ConcurrentHashMap<>();
    private final Map<String, AgentChannel> channels = new ConcurrentHashMap<>();

    public ConversationContext createConversation(String initiatorId, List<String> participants) {
        ConversationContext context = ConversationContext.start(initiatorId, participants);
        activeConversations.put(context.getConversationId(), context);
        log.info("CommunicationBus created conversation {} with initiator {}", context.getConversationId(), initiatorId);
        return context;
    }

    public void sendMessage(AgentMessage message) {
        if (message == null) return;

        log.debug("CommunicationBus routing message {} from {} to {} (Type: {})",
                message.getMessageId(), message.getSenderAgentId(), message.getRecipientAgentId(), message.getMessageType());

        if (message.getConversationId() != null && activeConversations.containsKey(message.getConversationId())) {
            activeConversations.get(message.getConversationId()).addMessage(message);
        }

        if ("*".equals(message.getRecipientAgentId())) {
            // Broadcast to all active agent inboxes
            agentInboxes.keySet().forEach(agentId -> deliverToInbox(agentId, message));
        } else if (message.getRecipientAgentId() != null) {
            deliverToInbox(message.getRecipientAgentId(), message);
        }
    }

    private void deliverToInbox(String agentId, AgentMessage message) {
        agentInboxes.computeIfAbsent(agentId, k -> new CopyOnWriteArrayList<>()).add(message);
    }

    public List<AgentMessage> fetchMessages(String agentId) {
        List<AgentMessage> inbox = agentInboxes.computeIfAbsent(agentId, k -> new CopyOnWriteArrayList<>());
        if (inbox.isEmpty()) {
            return Collections.emptyList();
        }
        List<AgentMessage> retrieved = new ArrayList<>(inbox);
        inbox.clear();
        return retrieved;
    }

    public Optional<ConversationContext> getConversation(String conversationId) {
        return Optional.ofNullable(activeConversations.get(conversationId));
    }

    public void cancelConversation(String conversationId, String reason) {
        ConversationContext context = activeConversations.get(conversationId);
        if (context != null) {
            context.setStatus(ConversationContext.ConversationStatus.CANCELLED);
            AgentMessage cancelMsg = AgentMessage.builder()
                    .senderAgentId("SYSTEM")
                    .recipientAgentId("*")
                    .conversationId(conversationId)
                    .messageType(AgentMessage.MessageType.CANCELLATION)
                    .payload(Map.of("reason", reason))
                    .build();
            sendMessage(cancelMsg);
            log.info("CommunicationBus cancelled conversation {}: {}", conversationId, reason);
        }
    }
}
