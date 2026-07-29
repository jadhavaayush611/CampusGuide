package com.campusguide.personal.ai.atlas.context.contributor;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.ContextContributor;
import com.campusguide.personal.ai.atlas.context.model.CampusContext;
import com.campusguide.personal.ai.atlas.context.service.CampusContextService;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Domain-aware ContextContributor for Campus metadata.
 * Depends on CampusContextService rather than repositories.
 */
@Component
@RequiredArgsConstructor
public class CampusContributor implements ContextContributor {

    private final CampusContextService campusContextService;

    @Override
    public String getName() {
        return "campus";
    }

    @Override
    public void contribute(AtlasChatRequest request, AtlasContext context) {
        String userId = context.getUserId();
        CampusContext campusContext = campusContextService.getCampusContext(userId, request);
        context.setCampusContext(campusContext);

        if (campusContext != null) {
            context.addContribution(getName(), campusContext);
            if (campusContext.getSummary() != null) {
                context.putPlaceholder("campus_summary", campusContext.getSummary());
            }
        }
    }
}
