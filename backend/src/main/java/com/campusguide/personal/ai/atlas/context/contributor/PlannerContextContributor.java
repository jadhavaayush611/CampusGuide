package com.campusguide.personal.ai.atlas.context.contributor;

import com.campusguide.personal.ai.atlas.context.service.PlannerContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Backward-compatible wrapper for PlannerContributor.
 */
@Component
@Primary
public class PlannerContextContributor extends PlannerContributor {

    public PlannerContextContributor() {
        super(new PlannerContextService(null));
    }

    @Autowired
    public PlannerContextContributor(PlannerContextService plannerContextService) {
        super(plannerContextService);
    }
}
