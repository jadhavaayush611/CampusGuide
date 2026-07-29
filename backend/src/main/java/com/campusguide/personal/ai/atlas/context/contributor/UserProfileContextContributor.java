package com.campusguide.personal.ai.atlas.context.contributor;

import com.campusguide.personal.ai.atlas.context.service.UserContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Backward-compatible wrapper for UserProfileContributor.
 */
@Component
@Primary
public class UserProfileContextContributor extends UserProfileContributor {

    public UserProfileContextContributor() {
        super(new UserContextService(null));
    }

    @Autowired
    public UserProfileContextContributor(UserContextService userContextService) {
        super(userContextService);
    }
}
