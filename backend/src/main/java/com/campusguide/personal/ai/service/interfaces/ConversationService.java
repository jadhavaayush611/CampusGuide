package com.campusguide.personal.ai.service.interfaces;

import com.campusguide.personal.ai.dto.request.CreateConversationRequest;
import com.campusguide.personal.ai.dto.request.UpdateConversationRequest;
import com.campusguide.personal.ai.dto.response.ConversationResponse;
import com.campusguide.personal.ai.dto.response.ConversationSummaryResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface ConversationService {
    ConversationResponse createConversation(UserDetails userDetails, CreateConversationRequest request);
    ConversationResponse renameConversation(UserDetails userDetails, String id, UpdateConversationRequest request);
    void deleteConversation(UserDetails userDetails, String id);
    ConversationResponse getConversation(UserDetails userDetails, String id);
    List<ConversationSummaryResponse> listConversations(UserDetails userDetails);
}
