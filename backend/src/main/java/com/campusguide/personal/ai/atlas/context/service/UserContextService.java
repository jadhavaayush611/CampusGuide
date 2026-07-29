package com.campusguide.personal.ai.atlas.context.service;

import com.campusguide.personal.ai.atlas.context.model.UserContext;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service responsible for querying, summarizing, and normalizing User Profile domain context.
 */
@Service
@Slf4j
public class UserContextService {

    private final UserRepository userRepository;

    public UserContextService(@Autowired(required = false) UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Queries and normalizes user profile context.
     *
     * @param userId optional target user ID
     * @param request chat request
     * @return normalized UserContext
     */
    public UserContext getUserContext(String userId, AtlasChatRequest request) {
        String effectiveUserId = StringUtils.hasText(userId) ? userId : "anonymous";
        String name = "Student";
        String email = null;
        String role = "STUDENT";
        String status = "ACTIVE";

        // Extract from request placeholders if present
        if (request != null && request.getContextPlaceholders() != null) {
            Object placeholderName = request.getContextPlaceholders().get("student_name");
            if (placeholderName != null && StringUtils.hasText(placeholderName.toString())) {
                name = placeholderName.toString();
            }
        }

        if (userRepository != null && StringUtils.hasText(userId)) {
            try {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User u = userOpt.get();
                    if (StringUtils.hasText(u.getUsername())) {
                        name = u.getUsername();
                    }
                    email = u.getEmail();
                    if (u.getRole() != null) {
                        role = u.getRole().name();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch user entity for userId [{}]: {}", userId, e.getMessage());
            }
        }

        Map<String, String> preferences = new HashMap<>();
        preferences.put("theme", "system");

        String summary = String.format("User Profile context summary for %s (ID: %s, Status: %s)", name, effectiveUserId, status);

        return UserContext.builder()
                .userId(effectiveUserId)
                .name(name)
                .email(email)
                .role(role)
                .status(status)
                .summary(summary)
                .preferences(preferences)
                .build();
    }
}
