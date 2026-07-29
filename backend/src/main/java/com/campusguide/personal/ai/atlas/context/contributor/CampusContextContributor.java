package com.campusguide.personal.ai.atlas.context.contributor;

import com.campusguide.personal.ai.atlas.context.service.CampusContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Backward-compatible wrapper for CampusContributor.
 */
@Component
@Primary
public class CampusContextContributor extends CampusContributor {

    public CampusContextContributor() {
        super(new CampusContextService(null));
    }

    @Autowired
    public CampusContextContributor(CampusContextService campusContextService) {
        super(campusContextService);
    }
}
