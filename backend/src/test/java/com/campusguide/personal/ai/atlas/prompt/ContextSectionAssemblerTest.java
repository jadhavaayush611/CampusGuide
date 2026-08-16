package com.campusguide.personal.ai.atlas.prompt;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.model.*;
import com.campusguide.personal.ai.atlas.prompt.model.ContextSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextSectionAssemblerTest {

    private ContextSectionAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new ContextSectionAssembler();
    }

    @Test
    void testAssembleSections_NullContext_ReturnsEmptyList() {
        List<ContextSection> sections = assembler.assembleSections(null);
        assertNotNull(sections);
        assertTrue(sections.isEmpty());
    }

    @Test
    void testAssembleSections_AllDomainsPresent_AssemblesWithCorrectPriorities() {
        AtlasContext context = AtlasContext.builder()
                .userContext(UserContext.builder().name("Alex").role("STUDENT").status("ACTIVE").summary("Profile summary").build())
                .academicContext(AcademicContext.builder().department("Computer Science").degreeProgram("B.S.").gpa(3.8).summary("Academic summary").build())
                .plannerContext(PlannerContext.builder().activeTasksCount(3).summary("Planner summary").build())
                .calendarContext(CalendarContext.builder().todayEventsCount(2).summary("Calendar summary").build())
                .campusContext(CampusContext.builder().location("Main Campus").summary("Campus summary").build())
                .build();

        List<ContextSection> sections = assembler.assembleSections(context);

        assertEquals(5, sections.size());

        // Check Priorities & Requirements
        ContextSection userSection = sections.get(0);
        assertEquals("--- USER PROFILE CONTEXT ---", userSection.getTitle());
        assertEquals("USER_PROFILE", userSection.getCategory());
        assertEquals(1, userSection.getPriority());
        assertTrue(userSection.isRequired());
        assertTrue(userSection.getEstimatedTokens() > 0);

        ContextSection academicSection = sections.get(1);
        assertEquals("--- ACADEMIC CONTEXT ---", academicSection.getTitle());
        assertEquals("ACADEMIC", academicSection.getCategory());
        assertEquals(2, academicSection.getPriority());
        assertFalse(academicSection.isRequired());

        ContextSection plannerSection = sections.get(2);
        assertEquals("--- PLANNER CONTEXT ---", plannerSection.getTitle());
        assertEquals(3, plannerSection.getPriority());

        ContextSection calendarSection = sections.get(3);
        assertEquals("--- CALENDAR CONTEXT ---", calendarSection.getTitle());
        assertEquals(4, calendarSection.getPriority());

        ContextSection campusSection = sections.get(4);
        assertEquals("--- CAMPUS CONTEXT ---", campusSection.getTitle());
        assertEquals(5, campusSection.getPriority());
    }

    @Test
    void testAssembleSections_WithEvidenceBundles_AssemblesSixSections() {
        AtlasContext context = AtlasContext.builder()
                .userContext(UserContext.builder().name("Alex").role("STUDENT").status("ACTIVE").summary("Profile summary").build())
                .build();
        
        com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence ev = com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence.builder()
                .contentSnippet("Ground floor contains ECS.")
                .build();
        context.addEvidenceBundle(com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle.builder()
                .targetDomain("campus")
                .evidences(List.of(ev))
                .build());

        List<ContextSection> sections = assembler.assembleSections(context);
        assertEquals(2, sections.size()); // User profile + Evidence

        ContextSection evidenceSection = sections.get(1);
        assertEquals("--- RETRIEVED VERIFIED EVIDENCE ---", evidenceSection.getTitle());
        assertEquals("EVIDENCE", evidenceSection.getCategory());
        assertEquals(1, evidenceSection.getPriority());
        assertTrue(evidenceSection.isRequired());
        assertTrue(evidenceSection.getContent().contains("Ground floor contains ECS."));
    }

    @Test
    void testAssembleSections_PartialContext_OmitsNullDomains() {
        AtlasContext context = AtlasContext.builder()
                .userContext(UserContext.builder().name("Sarah").build())
                .academicContext(AcademicContext.builder().department("Biology").build())
                .build();

        List<ContextSection> sections = assembler.assembleSections(context);

        assertEquals(2, sections.size());
        assertEquals("--- USER PROFILE CONTEXT ---", sections.get(0).getTitle());
        assertEquals("--- ACADEMIC CONTEXT ---", sections.get(1).getTitle());
    }
}
