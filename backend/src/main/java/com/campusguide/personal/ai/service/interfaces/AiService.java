package com.campusguide.personal.ai.service.interfaces;

import com.campusguide.personal.ai.dto.request.ChatRequest;
import com.campusguide.personal.ai.dto.response.ChatResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface AiService {
    ChatResponse chat(UserDetails userDetails, String conversationId, ChatRequest request);
}
