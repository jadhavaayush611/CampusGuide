package com.campusguide.personal.ai.atlas.service;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.orchestration.ConversationOrchestrator;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AtlasServiceImpl implements AtlasService {

    private final ConversationOrchestrator conversationOrchestrator;
    private final CurrentUserService currentUserService;

    @Override
    public AtlasChatResponse chat(AtlasChatRequest request) {
        log.info("Delegating unauthenticated/implicit chat request to ConversationOrchestrator");
        return conversationOrchestrator.orchestrate(request, (String) null);
    }

    @Override
    public AtlasChatResponse chat(AtlasChatRequest request, UserDetails userDetails) {
        log.info("Delegating authenticated chat request to ConversationOrchestrator");
        String userId = null;
        if (userDetails != null && currentUserService != null) {
            try {
                User user = currentUserService.getCurrentUser(userDetails);
                if (user != null) {
                    userId = user.getId();
                }
            } catch (Exception e) {
                log.warn("Could not resolve authenticated user from UserDetails: {}", e.getMessage());
            }
        }
        return conversationOrchestrator.orchestrate(request, userId);
    }
}
