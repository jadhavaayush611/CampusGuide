package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.orchestration.communication.AgentMessage;
import com.campusguide.personal.ai.atlas.orchestration.communication.CommunicationBus;
import com.campusguide.personal.ai.atlas.orchestration.communication.ConversationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CommunicationBusTest {

    private CommunicationBus bus;

    @BeforeEach
    void setUp() {
        bus = new CommunicationBus();
    }

    @Test
    void testDirectMessageDelivery() {
        AgentMessage msg = AgentMessage.request("agent_1", "agent_2", "conv_1", Map.of("key", "val"));
        bus.sendMessage(msg);

        List<AgentMessage> inbox = bus.fetchMessages("agent_2");
        assertEquals(1, inbox.size());
        assertEquals("agent_1", inbox.get(0).getSenderAgentId());
    }

    @Test
    void testBroadcastDelivery() {
        bus.fetchMessages("agent_a"); // initialize inbox
        bus.fetchMessages("agent_b"); // initialize inbox

        AgentMessage bcast = AgentMessage.broadcast("agent_master", "conv_bcast", Map.of("alert", "sync"));
        bus.sendMessage(bcast);

        assertEquals(1, bus.fetchMessages("agent_a").size());
        assertEquals(1, bus.fetchMessages("agent_b").size());
    }

    @Test
    void testConversationManagementAndCancellation() {
        ConversationContext conv = bus.createConversation("agent_1", List.of("agent_2", "agent_3"));

        assertNotNull(conv);
        assertEquals(ConversationContext.ConversationStatus.ACTIVE, conv.getStatus());

        bus.cancelConversation(conv.getConversationId(), "User cancelled workflow");

        Optional<ConversationContext> updated = bus.getConversation(conv.getConversationId());
        assertTrue(updated.isPresent());
        assertEquals(ConversationContext.ConversationStatus.CANCELLED, updated.get().getStatus());
    }
}
