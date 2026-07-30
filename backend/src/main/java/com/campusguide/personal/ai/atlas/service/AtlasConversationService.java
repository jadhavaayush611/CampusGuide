package com.campusguide.personal.ai.atlas.service;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationCreateRequest;
import com.campusguide.personal.ai.atlas.dto.ConversationHistoryResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationSummaryResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationUpdateRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface AtlasConversationService {

    ConversationResponse createConversation(ConversationCreateRequest request, UserDetails userDetails);

    List<ConversationResponse> getUserConversations(UserDetails userDetails);

    ConversationResponse getConversation(String id, UserDetails userDetails);

    ConversationResponse updateConversation(String id, ConversationUpdateRequest request, UserDetails userDetails);

    ConversationResponse archiveConversation(String id, UserDetails userDetails);

    ConversationResponse restoreConversation(String id, UserDetails userDetails);

    ConversationResponse renameConversation(String id, String newTitle, UserDetails userDetails);

    ConversationSummaryResponse getConversationSummary(String id, UserDetails userDetails);

    AtlasChatResponse continueConversation(String id, AtlasChatRequest request, UserDetails userDetails);

    void deleteConversation(String id, UserDetails userDetails);

    ConversationHistoryResponse getConversationHistory(String id, UserDetails userDetails);
}
