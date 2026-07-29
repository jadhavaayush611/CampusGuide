package com.campusguide.personal.ai.atlas.prompt;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.model.*;
import com.campusguide.personal.ai.atlas.prompt.model.ContextSection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms an aggregate AtlasContext model into a list of modular ContextSection instances,
 * assigning priority levels and required flags for token budgeting.
 */
@Component
public class ContextSectionAssembler {

    public List<ContextSection> assembleSections(AtlasContext atlasContext) {
        List<ContextSection> sections = new ArrayList<>();
        if (atlasContext == null) {
            return sections;
        }

        // 1. User Profile Section (Priority 1 - Required)
        if (atlasContext.getUserContext() != null) {
            UserContext uc = atlasContext.getUserContext();
            StringBuilder sb = new StringBuilder();
            if (uc.getName() != null) sb.append("Name: ").append(uc.getName()).append("\n");
            if (uc.getRole() != null) sb.append("Role: ").append(uc.getRole()).append("\n");
            if (uc.getStatus() != null) sb.append("Status: ").append(uc.getStatus()).append("\n");
            if (uc.getSummary() != null) sb.append("Summary: ").append(uc.getSummary()).append("\n");

            String content = sb.toString().trim();
            if (!content.isBlank()) {
                sections.add(ContextSection.of("--- USER PROFILE CONTEXT ---", content, "USER_PROFILE", 1, true));
            }
        }

        // 2. Academic Section (Priority 2 - Optional)
        if (atlasContext.getAcademicContext() != null) {
            AcademicContext ac = atlasContext.getAcademicContext();
            StringBuilder sb = new StringBuilder();
            if (ac.getDepartment() != null) sb.append("Department: ").append(ac.getDepartment()).append("\n");
            if (ac.getDegreeProgram() != null) sb.append("Program: ").append(ac.getDegreeProgram()).append("\n");
            if (ac.getAcademicStanding() != null) sb.append("Standing: ").append(ac.getAcademicStanding()).append("\n");
            if (ac.getGpa() != null) sb.append("GPA: ").append(ac.getGpa()).append("\n");
            if (ac.getCompletedCredits() != null) sb.append("Credits Completed: ").append(ac.getCompletedCredits()).append("\n");
            if (ac.getCurrentCourses() != null && !ac.getCurrentCourses().isEmpty()) {
                sb.append("Current Courses: ").append(String.join(", ", ac.getCurrentCourses())).append("\n");
            }
            if (ac.getSummary() != null) sb.append("Summary: ").append(ac.getSummary()).append("\n");

            String content = sb.toString().trim();
            if (!content.isBlank()) {
                sections.add(ContextSection.of("--- ACADEMIC CONTEXT ---", content, "ACADEMIC", 2, false));
            }
        }

        // 3. Planner Section (Priority 3 - Optional)
        if (atlasContext.getPlannerContext() != null) {
            PlannerContext pc = atlasContext.getPlannerContext();
            StringBuilder sb = new StringBuilder();
            sb.append("Active Tasks: ").append(pc.getActiveTasksCount())
                    .append(", Overdue: ").append(pc.getOverdueTasksCount())
                    .append(", Completed: ").append(pc.getCompletedTasksCount()).append("\n");
            if (pc.getTopTasks() != null && !pc.getTopTasks().isEmpty()) {
                sb.append("Top Tasks:\n");
                for (PlannerContext.TaskSummary task : pc.getTopTasks()) {
                    sb.append("  - ").append(task.getTitle());
                    if (task.getDueDate() != null) sb.append(" (Due: ").append(task.getDueDate()).append(")");
                    if (task.getStatus() != null) sb.append(" [").append(task.getStatus()).append("]");
                    sb.append("\n");
                }
            }
            if (pc.getSummary() != null) sb.append("Summary: ").append(pc.getSummary()).append("\n");

            String content = sb.toString().trim();
            if (!content.isBlank()) {
                sections.add(ContextSection.of("--- PLANNER CONTEXT ---", content, "PLANNER", 3, false));
            }
        }

        // 4. Calendar Section (Priority 4 - Optional)
        if (atlasContext.getCalendarContext() != null) {
            CalendarContext cc = atlasContext.getCalendarContext();
            StringBuilder sb = new StringBuilder();
            sb.append("Today Events Count: ").append(cc.getTodayEventsCount())
                    .append(", Upcoming Events Count: ").append(cc.getUpcomingEventsCount()).append("\n");
            if (cc.getTodayEvents() != null && !cc.getTodayEvents().isEmpty()) {
                sb.append("Today Events:\n");
                for (CalendarContext.EventSummary event : cc.getTodayEvents()) {
                    sb.append("  - ").append(event.getTitle());
                    if (event.getStartTime() != null) sb.append(" at ").append(event.getStartTime());
                    if (event.getLocation() != null) sb.append(" (").append(event.getLocation()).append(")");
                    sb.append("\n");
                }
            }
            if (cc.getSummary() != null) sb.append("Summary: ").append(cc.getSummary()).append("\n");

            String content = sb.toString().trim();
            if (!content.isBlank()) {
                sections.add(ContextSection.of("--- CALENDAR CONTEXT ---", content, "CALENDAR", 4, false));
            }
        }

        // 5. Campus Section (Priority 5 - Optional)
        if (atlasContext.getCampusContext() != null) {
            CampusContext cam = atlasContext.getCampusContext();
            StringBuilder sb = new StringBuilder();
            if (cam.getLocation() != null) sb.append("Location: ").append(cam.getLocation()).append("\n");
            sb.append("Active Events: ").append(cam.getActiveEventsCount())
                    .append(", Active Notices: ").append(cam.getActiveNoticesCount()).append("\n");
            if (cam.getAnnouncements() != null && !cam.getAnnouncements().isEmpty()) {
                sb.append("Announcements:\n");
                for (String ann : cam.getAnnouncements()) {
                    sb.append("  - ").append(ann).append("\n");
                }
            }
            if (cam.getSummary() != null) sb.append("Summary: ").append(cam.getSummary()).append("\n");

            String content = sb.toString().trim();
            if (!content.isBlank()) {
                sections.add(ContextSection.of("--- CAMPUS CONTEXT ---", content, "CAMPUS", 5, false));
            }
        }

        return sections;
    }
}
