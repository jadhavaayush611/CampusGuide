package com.campusguide.personal.ai.service.interfaces;

import com.campusguide.personal.ai.dto.gateway.GatewayMessage;
import java.util.List;

public interface ConversationContextBuilder {
    List<GatewayMessage> buildHistoryContext(String conversationId);
}
