package com.campusguide.personal.ai.atlas.context.contributor;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.ContextContributor;
import com.campusguide.personal.ai.atlas.context.model.PlannerContext;
import com.campusguide.personal.ai.atlas.context.service.PlannerContextService;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Domain-aware ContextContributor for Planner.
 * Depends on PlannerContextService rather than repositories.
 */
@Component
@RequiredArgsConstructor
public class PlannerContributor implements ContextContributor {

    private final PlannerContextService plannerContextService;

    @Override
    public String getName() {
        return "planner";
    }

    @Override
    public void contribute(AtlasChatRequest request, AtlasContext context) {
        String userId = context.getUserId();
        PlannerContext plannerContext = plannerContextService.getPlannerContext(userId, request);
        context.setPlannerContext(plannerContext);

        if (plannerContext != null) {
            context.addContribution(getName(), plannerContext);
            if (plannerContext.getSummary() != null) {
                context.putPlaceholder("planner_summary", plannerContext.getSummary());
            }
        }
    }
}
