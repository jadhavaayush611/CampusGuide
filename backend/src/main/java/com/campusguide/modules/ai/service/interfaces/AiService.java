package com.campusguide.modules.ai.service.interfaces;

import com.campusguide.modules.ai.dto.request.ChatRequest;
import com.campusguide.modules.ai.dto.response.ChatResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface AiService {
    ChatResponse chat(UserDetails userDetails, String conversationId, ChatRequest request);
}
