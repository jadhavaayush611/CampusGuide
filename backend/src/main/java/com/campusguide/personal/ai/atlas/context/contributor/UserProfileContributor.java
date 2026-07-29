package com.campusguide.personal.ai.atlas.context.contributor;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.ContextContributor;
import com.campusguide.personal.ai.atlas.context.model.UserContext;
import com.campusguide.personal.ai.atlas.context.service.UserContextService;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Domain-aware ContextContributor for User Profile.
 * Depends on UserContextService rather than repositories.
 */
@Component
@RequiredArgsConstructor
public class UserProfileContributor implements ContextContributor {

    private final UserContextService userContextService;

    @Override
    public String getName() {
        return "userProfile";
    }

    @Override
    public void contribute(AtlasChatRequest request, AtlasContext context) {
        String userId = context.getUserId();
        UserContext userContext = userContextService.getUserContext(userId, request);
        context.setUserContext(userContext);

        if (userContext != null) {
            context.addContribution(getName(), userContext);
            if (userContext.getName() != null && (request.getContextPlaceholders() == null || !request.getContextPlaceholders().containsKey("student_name"))) {
                context.putPlaceholder("student_name", userContext.getName());
            }
            if (userContext.getSummary() != null) {
                context.putPlaceholder("user_profile_summary", userContext.getSummary());
            }
        }
    }
}
