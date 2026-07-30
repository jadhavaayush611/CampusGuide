package com.campusguide.personal.ai.atlas.streaming;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AtlasStreamingService {
    SseEmitter streamChat(AtlasChatRequest request, String lastEventId, UserDetails userDetails);
}
