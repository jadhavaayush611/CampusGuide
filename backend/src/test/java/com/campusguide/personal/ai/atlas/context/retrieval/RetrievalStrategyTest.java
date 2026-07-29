package com.campusguide.personal.ai.atlas.context.retrieval;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.contributor.AcademicContributor;
import com.campusguide.personal.ai.atlas.context.contributor.CalendarContributor;
import com.campusguide.personal.ai.atlas.context.contributor.CampusContributor;
import com.campusguide.personal.ai.atlas.context.contributor.PlannerContributor;
import com.campusguide.personal.ai.atlas.context.contributor.UserProfileContributor;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryDomain;
import com.campusguide.personal.ai.atlas.context.query.QueryIntent;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class RetrievalStrategyTest {

    @Mock private UserProfileContributor userProfileContributor;
    @Mock private AcademicContributor academicContributor;
    @Mock private PlannerContributor plannerContributor;
    @Mock private CalendarContributor calendarContributor;
    @Mock private CampusContributor campusContributor;

    private UserRetrievalStrategy userStrategy;
    private AcademicRetrievalStrategy academicStrategy;
    private PlannerRetrievalStrategy plannerStrategy;
    private CalendarRetrievalStrategy calendarStrategy;
    private CampusRetrievalStrategy campusStrategy;

    private RetrievalPolicy policy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userStrategy = new UserRetrievalStrategy(userProfileContributor);
        academicStrategy = new AcademicRetrievalStrategy(academicContributor);
        plannerStrategy = new PlannerRetrievalStrategy(plannerContributor);
        calendarStrategy = new CalendarRetrievalStrategy(calendarContributor);
        campusStrategy = new CampusRetrievalStrategy(campusContributor);
        policy = new RetrievalPolicy();
    }

    @Test
    @DisplayName("UserRetrievalStrategy should support execution when policy mandates alwaysRetrieveUserProfile")
    void testUserStrategy_Supports() {
        QueryContext qc = QueryContext.builder().domainClassification(QueryDomain.CAMPUS).intent(QueryIntent.CAMPUS_NAVIGATION).confidenceScore(0.90).build();
        assertTrue(userStrategy.supports(qc, policy));
        assertEquals(0.85, userStrategy.calculateRelevance(qc));
    }

    @Test
    @DisplayName("AcademicRetrievalStrategy should support execution when query domain is ACADEMIC")
    void testAcademicStrategy_Supports() {
        QueryContext academicQc = QueryContext.builder().domainClassification(QueryDomain.ACADEMIC).intent(QueryIntent.ACADEMIC_INQUIRY).confidenceScore(0.85).build();
        QueryContext campusQc = QueryContext.builder().domainClassification(QueryDomain.CAMPUS).intent(QueryIntent.CAMPUS_NAVIGATION).confidenceScore(0.85).build();

        assertTrue(academicStrategy.supports(academicQc, policy));
        assertFalse(academicStrategy.supports(campusQc, policy));
    }

    @Test
    @DisplayName("PlannerRetrievalStrategy should support execution when query domain is PLANNER")
    void testPlannerStrategy_Supports() {
        QueryContext plannerQc = QueryContext.builder().domainClassification(QueryDomain.PLANNER).intent(QueryIntent.PLANNER_LOOKUP).confidenceScore(0.85).build();
        QueryContext campusQc = QueryContext.builder().domainClassification(QueryDomain.CAMPUS).intent(QueryIntent.CAMPUS_NAVIGATION).confidenceScore(0.85).build();

        assertTrue(plannerStrategy.supports(plannerQc, policy));
        assertFalse(plannerStrategy.supports(campusQc, policy));
    }

    @Test
    @DisplayName("CalendarRetrievalStrategy should support execution when query domain is CALENDAR")
    void testCalendarStrategy_Supports() {
        QueryContext calQc = QueryContext.builder().domainClassification(QueryDomain.CALENDAR).intent(QueryIntent.CALENDAR_EVENT).confidenceScore(0.85).build();
        assertTrue(calendarStrategy.supports(calQc, policy));
    }

    @Test
    @DisplayName("CampusRetrievalStrategy should support execution when query domain is CAMPUS")
    void testCampusStrategy_Supports() {
        QueryContext campusQc = QueryContext.builder().domainClassification(QueryDomain.CAMPUS).intent(QueryIntent.CAMPUS_NAVIGATION).confidenceScore(0.85).build();
        assertTrue(campusStrategy.supports(campusQc, policy));
    }

    @Test
    @DisplayName("Strategy retrieval method delegates to underlying contributor")
    void testStrategy_Retrieve() {
        AtlasChatRequest request = AtlasChatRequest.builder().prompt("Where is the library?").build();
        RetrievalContext rc = RetrievalContext.builder().request(request).policy(policy).build();
        AtlasContext ac = new AtlasContext();

        campusStrategy.retrieve(rc, ac);
        verify(campusContributor).contribute(any(), any());
    }
}
