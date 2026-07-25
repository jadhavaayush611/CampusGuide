package com.campusguide.personal.ai.service.interfaces;

import com.campusguide.personal.ai.dto.request.SendMessageRequest;
import com.campusguide.personal.ai.dto.response.ConversationHistoryResponse;
import com.campusguide.personal.ai.dto.response.MessageResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface MessageService {
    MessageResponse saveMessage(UserDetails userDetails, String conversationId, SendMessageRequest request);
    ConversationHistoryResponse getConversationHistory(UserDetails userDetails, String conversationId);
    void deleteConversationMessages(UserDetails userDetails, String conversationId);
}
