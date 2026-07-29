package com.campusguide.personal.ai.atlas.context.contributor;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.ContextContributor;
import com.campusguide.personal.ai.atlas.context.model.AcademicContext;
import com.campusguide.personal.ai.atlas.context.service.AcademicContextService;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Domain-aware ContextContributor for Academic status.
 * Depends on AcademicContextService rather than repositories.
 */
@Component
@RequiredArgsConstructor
public class AcademicContributor implements ContextContributor {

    private final AcademicContextService academicContextService;

    @Override
    public String getName() {
        return "academic";
    }

    @Override
    public void contribute(AtlasChatRequest request, AtlasContext context) {
        String userId = context.getUserId();
        AcademicContext academicContext = academicContextService.getAcademicContext(userId, request);
        context.setAcademicContext(academicContext);

        if (academicContext != null) {
            context.addContribution(getName(), academicContext);
            if (academicContext.getDepartment() != null && (request.getContextPlaceholders() == null || !request.getContextPlaceholders().containsKey("department"))) {
                context.putPlaceholder("department", academicContext.getDepartment());
            }
            if (academicContext.getSummary() != null) {
                context.putPlaceholder("academic_summary", academicContext.getSummary());
            }
        }
    }
}
