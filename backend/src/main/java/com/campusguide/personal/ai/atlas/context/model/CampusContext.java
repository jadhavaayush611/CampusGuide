package com.campusguide.personal.ai.atlas.context.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Strongly-typed domain context model for Campus.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampusContext {

    private String location;
    private int activeEventsCount;
    private int activeNoticesCount;

    @Builder.Default
    private List<String> announcements = Collections.emptyList();

    private String summary;
}
