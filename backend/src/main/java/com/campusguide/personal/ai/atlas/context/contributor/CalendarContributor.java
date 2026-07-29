package com.campusguide.personal.ai.atlas.context.contributor;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.ContextContributor;
import com.campusguide.personal.ai.atlas.context.model.CalendarContext;
import com.campusguide.personal.ai.atlas.context.service.CalendarContextService;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Domain-aware ContextContributor for Calendar.
 * Depends on CalendarContextService rather than repositories.
 */
@Component
@RequiredArgsConstructor
public class CalendarContributor implements ContextContributor {

    private final CalendarContextService calendarContextService;

    @Override
    public String getName() {
        return "calendar";
    }

    @Override
    public void contribute(AtlasChatRequest request, AtlasContext context) {
        String userId = context.getUserId();
        CalendarContext calendarContext = calendarContextService.getCalendarContext(userId, request);
        context.setCalendarContext(calendarContext);

        if (calendarContext != null) {
            context.addContribution(getName(), calendarContext);
            if (calendarContext.getSummary() != null) {
                context.putPlaceholder("calendar_summary", calendarContext.getSummary());
            }
        }
    }
}
