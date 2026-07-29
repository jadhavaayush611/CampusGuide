package com.campusguide.personal.ai.atlas.context.contributor;

import com.campusguide.personal.ai.atlas.context.service.CalendarContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Backward-compatible wrapper for CalendarContributor.
 */
@Component
@Primary
public class CalendarContextContributor extends CalendarContributor {

    public CalendarContextContributor() {
        super(new CalendarContextService(null));
    }

    @Autowired
    public CalendarContextContributor(CalendarContextService calendarContextService) {
        super(calendarContextService);
    }
}
