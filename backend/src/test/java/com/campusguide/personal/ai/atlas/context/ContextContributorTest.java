package com.campusguide.personal.ai.atlas.context;

import com.campusguide.personal.ai.atlas.context.contributor.*;
import com.campusguide.personal.ai.atlas.context.model.*;
import com.campusguide.personal.ai.atlas.context.service.*;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextContributorTest {

    @Mock private UserContextService userContextService;
    @Mock private PlannerContextService plannerContextService;
    @Mock private CalendarContextService calendarContextService;
    @Mock private AcademicContextService academicContextService;
    @Mock private CampusContextService campusContextService;

    private AtlasContext context;
    private AtlasChatRequest request;

    @BeforeEach
    void setUp() {
        context = new AtlasContext("conv-123", "user-456");
        request = AtlasChatRequest.builder()
                .conversationId("conv-123")
                .build();
    }

    @Test
    void testUserProfileContributor() {
        UserContext uc = UserContext.builder().userId("user-456").name("Alex").status("ACTIVE").summary("User profile summary").build();
        when(userContextService.getUserContext(eq("user-456"), any())).thenReturn(uc);

        UserProfileContributor contributor = new UserProfileContributor(userContextService);
        assertEquals("userProfile", contributor.getName());

        contributor.contribute(request, context);

        assertNotNull(context.getUserContext());
        assertEquals("Alex", context.getUserContext().getName());
        assertEquals("Alex", context.getMergedPlaceholders().get("student_name"));
    }

    @Test
    void testPlannerContributor() {
        PlannerContext pc = PlannerContext.builder().activeTasksCount(2).summary("Planner summary").build();
        when(plannerContextService.getPlannerContext(eq("user-456"), any())).thenReturn(pc);

        PlannerContributor contributor = new PlannerContributor(plannerContextService);
        assertEquals("planner", contributor.getName());

        contributor.contribute(request, context);

        assertNotNull(context.getPlannerContext());
        assertEquals(2, context.getPlannerContext().getActiveTasksCount());
    }

    @Test
    void testCalendarContributor() {
        CalendarContext cc = CalendarContext.builder().todayEventsCount(1).summary("Calendar summary").build();
        when(calendarContextService.getCalendarContext(eq("user-456"), any())).thenReturn(cc);

        CalendarContributor contributor = new CalendarContributor(calendarContextService);
        assertEquals("calendar", contributor.getName());

        contributor.contribute(request, context);

        assertNotNull(context.getCalendarContext());
        assertEquals(1, context.getCalendarContext().getTodayEventsCount());
    }

    @Test
    void testAcademicContributor() {
        AcademicContext ac = AcademicContext.builder().department("Computer Science").gpa(3.8).summary("Academic summary").build();
        when(academicContextService.getAcademicContext(eq("user-456"), any())).thenReturn(ac);

        AcademicContributor contributor = new AcademicContributor(academicContextService);
        assertEquals("academic", contributor.getName());

        contributor.contribute(request, context);

        assertNotNull(context.getAcademicContext());
        assertEquals("Computer Science", context.getAcademicContext().getDepartment());
        assertEquals("Computer Science", context.getMergedPlaceholders().get("department"));
    }

    @Test
    void testCampusContributor() {
        CampusContext cam = CampusContext.builder().location("Main Campus").summary("Campus summary").build();
        when(campusContextService.getCampusContext(eq("user-456"), any())).thenReturn(cam);

        CampusContributor contributor = new CampusContributor(campusContextService);
        assertEquals("campus", contributor.getName());

        contributor.contribute(request, context);

        assertNotNull(context.getCampusContext());
        assertEquals("Main Campus", context.getCampusContext().getLocation());
    }

    @Test
    void testExplicitRequestPlaceholdersOverriddenByContextEngine() {
        AtlasChatRequest customRequest = AtlasChatRequest.builder()
                .contextPlaceholders(Map.of("student_name", "Alex", "department", "Data Science"))
                .build();

        UserContext uc = UserContext.builder().name("Alex").build();
        AcademicContext ac = AcademicContext.builder().department("Data Science").build();

        when(userContextService.getUserContext(any(), any())).thenReturn(uc);
        when(academicContextService.getAcademicContext(any(), any())).thenReturn(ac);

        UserProfileContributor profileContrib = new UserProfileContributor(userContextService);
        AcademicContributor academicContrib = new AcademicContributor(academicContextService);

        context.putPlaceholders(customRequest.getContextPlaceholders());
        profileContrib.contribute(customRequest, context);
        academicContrib.contribute(customRequest, context);

        assertEquals("Alex", context.getMergedPlaceholders().get("student_name"));
        assertEquals("Data Science", context.getMergedPlaceholders().get("department"));
    }
}
