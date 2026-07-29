package com.campusguide.personal.ai.atlas.context.contributor;

import com.campusguide.personal.ai.atlas.context.service.AcademicContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Backward-compatible wrapper for AcademicContributor.
 */
@Component
@Primary
public class AcademicContextContributor extends AcademicContributor {

    public AcademicContextContributor() {
        super(new AcademicContextService(null));
    }

    @Autowired
    public AcademicContextContributor(AcademicContextService academicContextService) {
        super(academicContextService);
    }
}
