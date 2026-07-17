package com.campusguide.modules.ai.service.interfaces;

import com.campusguide.modules.ai.dto.gateway.GatewayMessage;
import java.util.List;

public interface ConversationContextBuilder {
    List<GatewayMessage> buildHistoryContext(String conversationId);
}
