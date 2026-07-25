package com.campusguide.academic.service;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.academic.dto.AcademicDashboardResponse;
import com.campusguide.academic.dto.AcademicProgressResponse;
import com.campusguide.academic.dto.RecommendedSemesterResponse;
import com.campusguide.academic.course.dto.CourseResponse;
import com.campusguide.academic.course.entity.Course;
import com.campusguide.academic.course.repository.CourseRepository;
import com.campusguide.academic.progress.entity.StudentProgress;
import com.campusguide.academic.progress.repository.StudentProgressRepository;
import com.campusguide.academic.roadmap.entity.Roadmap;
import com.campusguide.academic.roadmap.repository.RoadmapRepository;
import com.campusguide.academic.semesterplanner.entity.SemesterPlan;
import com.campusguide.academic.semesterplanner.repository.SemesterPlanRepository;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicService {

    private final UserRepository userRepository;
    private final StudentProgressRepository studentProgressRepository;
    private final RoadmapRepository roadmapRepository;
    private final SemesterPlanRepository semesterPlanRepository;
    private final CourseRepository courseRepository;

    /**
     * Retrieves the Academic Dashboard for the authenticated user.
     *
     * @param userDetails the authenticated user details
     * @return AcademicDashboardResponse containing all dashboard info
     */
    public AcademicDashboardResponse getDashboard(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        StudentProgress progress = studentProgressRepository.findByStudentId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student progress not found for student: " + user.getId()));

        if (progress.getRoadmapId() == null) {
            throw new ResourceNotFoundException("Roadmap ID not associated with student progress");
        }

        Roadmap roadmap = roadmapRepository.findById(progress.getRoadmapId())
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + progress.getRoadmapId()));

        if (Boolean.TRUE.equals(roadmap.getIsDeleted())) {
            throw new ResourceNotFoundException("Roadmap not found with ID: " + progress.getRoadmapId());
        }

        List<SemesterPlan> plans = semesterPlanRepository.findByStudentIdOrderBySemesterNumberAsc(user.getId());

        // Calculations
        int requiredCredits = roadmap.getTotalCredits() != null ? roadmap.getTotalCredits() : 0;
        int earnedCredits = progress.getTotalCreditsEarned() != null ? progress.getTotalCreditsEarned() : 0;
        int remainingCredits = Math.max(0, requiredCredits - earnedCredits);
        double completionPercentage = calculateCompletionPercentage(earnedCredits, requiredCredits);

        int plannedCredits = plans.stream()
                .mapToInt(p -> p.getTotalPlannedCredits() != null ? p.getTotalPlannedCredits() : 0)
                .sum();

        Integer currentSemester = progress.getCurrentSemester();
        boolean finalizedSemesterPlan = false;
        if (currentSemester != null) {
            finalizedSemesterPlan = plans.stream()
                    .filter(p -> currentSemester.equals(p.getSemesterNumber()))
                    .findFirst()
                    .map(p -> Boolean.TRUE.equals(p.getFinalized()))
                    .orElse(false);
        }

        List<String> completedIds = progress.getCompletedCourseIds() != null ? progress.getCompletedCourseIds() : new ArrayList<>();
        List<String> plannedIds = plans.stream()
                .flatMap(p -> p.getPlannedCourseIds() != null ? p.getPlannedCourseIds().stream() : new ArrayList<String>().stream())
                .distinct()
                .toList();

        List<Course> allRoadmapCourses = courseRepository.findByDepartmentAndActiveTrueOrderByCourseCodeAsc(roadmap.getDepartment());

        List<CourseResponse> completedCourses = courseRepository.findAllById(completedIds).stream()
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .map(this::toCourseResponse)
                .toList();

        List<CourseResponse> remainingCourses = allRoadmapCourses.stream()
                .filter(c -> !completedIds.contains(c.getId()) && !plannedIds.contains(c.getId()))
                .map(this::toCourseResponse)
                .toList();

        return AcademicDashboardResponse.builder()
                .roadmapTitle(roadmap.getTitle())
                .degreeProgram(roadmap.getDegreeProgram())
                .department(roadmap.getDepartment())
                .currentSemester(currentSemester)
                .totalCreditsRequired(requiredCredits)
                .totalCreditsEarned(earnedCredits)
                .remainingCredits(remainingCredits)
                .completionPercentage(completionPercentage)
                .currentGpa(progress.getCurrentGpa())
                .graduationEligible(progress.getGraduationEligible())
                .plannedCredits(plannedCredits)
                .finalizedSemesterPlan(finalizedSemesterPlan)
                .completedCourses(completedCourses)
                .remainingCourses(remainingCourses)
                .build();
    }

    /**
     * Retrieves the Academic Progress data for the authenticated user.
     *
     * @param userDetails the authenticated user details
     * @return AcademicProgressResponse containing progress metrics
     */
    public AcademicProgressResponse getProgress(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        StudentProgress progress = studentProgressRepository.findByStudentId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student progress not found for student: " + user.getId()));

        if (progress.getRoadmapId() == null) {
            throw new ResourceNotFoundException("Roadmap ID not associated with student progress");
        }

        Roadmap roadmap = roadmapRepository.findById(progress.getRoadmapId())
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + progress.getRoadmapId()));

        if (Boolean.TRUE.equals(roadmap.getIsDeleted())) {
            throw new ResourceNotFoundException("Roadmap not found with ID: " + progress.getRoadmapId());
        }

        List<SemesterPlan> plans = semesterPlanRepository.findByStudentIdOrderBySemesterNumberAsc(user.getId());

        List<String> completedCourseIds = progress.getCompletedCourseIds() != null ? progress.getCompletedCourseIds() : new ArrayList<>();
        List<String> plannedCourseIds = plans.stream()
                .flatMap(p -> p.getPlannedCourseIds() != null ? p.getPlannedCourseIds().stream() : new ArrayList<String>().stream())
                .distinct()
                .toList();

        List<Course> allRoadmapCourses = courseRepository.findByDepartmentAndActiveTrueOrderByCourseCodeAsc(roadmap.getDepartment());
        List<String> remainingCourseIds = allRoadmapCourses.stream()
                .map(Course::getId)
                .filter(id -> !completedCourseIds.contains(id) && !plannedCourseIds.contains(id))
                .toList();

        int requiredCredits = roadmap.getTotalCredits() != null ? roadmap.getTotalCredits() : 0;
        int earnedCredits = progress.getTotalCreditsEarned() != null ? progress.getTotalCreditsEarned() : 0;
        int creditsRemaining = Math.max(0, requiredCredits - earnedCredits);
        double completionPercentage = calculateCompletionPercentage(earnedCredits, requiredCredits);

        return AcademicProgressResponse.builder()
                .completedCourseIds(completedCourseIds)
                .plannedCourseIds(plannedCourseIds)
                .remainingCourseIds(remainingCourseIds)
                .creditsEarned(earnedCredits)
                .creditsRemaining(creditsRemaining)
                .completionPercentage(completionPercentage)
                .build();
    }

    /**
     * Generates course recommendations for the targeted semester.
     *
     * @param userDetails the authenticated user details
     * @param semesterNumber the semester number (optional, defaults to next semester)
     * @return RecommendedSemesterResponse containing recommendations and warnings
     */
    public RecommendedSemesterResponse getRecommendedSemester(UserDetails userDetails, Integer semesterNumber) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        StudentProgress progress = studentProgressRepository.findByStudentId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student progress not found for student: " + user.getId()));

        if (progress.getRoadmapId() == null) {
            throw new ResourceNotFoundException("Roadmap ID not associated with student progress");
        }

        Roadmap roadmap = roadmapRepository.findById(progress.getRoadmapId())
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with ID: " + progress.getRoadmapId()));

        if (Boolean.TRUE.equals(roadmap.getIsDeleted())) {
            throw new ResourceNotFoundException("Roadmap not found with ID: " + progress.getRoadmapId());
        }

        int targetSemester = (semesterNumber != null) ? semesterNumber : (progress.getCurrentSemester() != null ? progress.getCurrentSemester() + 1 : 1);
        if (targetSemester <= 0) {
            throw new BadRequestException("Semester number must be greater than 0");
        }

        List<Course> targetSemesterCourses = courseRepository.findBySemesterAndActiveTrueOrderByCourseCodeAsc(targetSemester);
        List<Course> candidateCourses = targetSemesterCourses.stream()
                .filter(c -> c.getDepartment() != null && c.getDepartment().equalsIgnoreCase(roadmap.getDepartment()))
                .toList();

        List<String> completedCourseIds = progress.getCompletedCourseIds() != null ? progress.getCompletedCourseIds() : new ArrayList<>();

        // Performance Optimization: Batch fetch missing prerequisite courses to avoid N+1 database queries
        List<String> missingPrereqIdsToFetch = candidateCourses.stream()
                .filter(c -> !completedCourseIds.contains(c.getId()))
                .flatMap(c -> c.getPrerequisiteCourseIds() != null ? c.getPrerequisiteCourseIds().stream() : java.util.stream.Stream.empty())
                .filter(prereqId -> !completedCourseIds.contains(prereqId))
                .distinct()
                .toList();

        java.util.Map<String, String> prereqIdToCodeMap = new java.util.HashMap<>();
        if (!missingPrereqIdsToFetch.isEmpty()) {
            List<Course> prereqCourses = courseRepository.findAllById(missingPrereqIdsToFetch);
            for (Course c : prereqCourses) {
                prereqIdToCodeMap.put(c.getId(), c.getCourseCode());
            }
        }

        List<String> recommendedCourseIds = new ArrayList<>();
        List<String> prerequisiteWarnings = new ArrayList<>();
        int totalCredits = 0;

        for (Course course : candidateCourses) {
            if (completedCourseIds.contains(course.getId())) {
                continue;
            }

            List<String> prereqs = course.getPrerequisiteCourseIds() != null ? course.getPrerequisiteCourseIds() : new ArrayList<>();
            List<String> missingPrereqs = new ArrayList<>();

            for (String prereqId : prereqs) {
                if (!completedCourseIds.contains(prereqId)) {
                    String prereqCode = prereqIdToCodeMap.getOrDefault(prereqId, prereqId);
                    missingPrereqs.add(prereqCode);
                }
            }

            if (missingPrereqs.isEmpty()) {
                recommendedCourseIds.add(course.getId());
                totalCredits += course.getCredits() != null ? course.getCredits() : 0;
            } else {
                prerequisiteWarnings.add("Course " + course.getCourseCode() + " requires prerequisite " + String.join(", ", missingPrereqs) + " which is not completed.");
            }
        }

        return RecommendedSemesterResponse.builder()
                .semesterNumber(targetSemester)
                .recommendedCourseIds(recommendedCourseIds)
                .totalCredits(totalCredits)
                .prerequisiteWarnings(prerequisiteWarnings)
                .build();
    }

    private double calculateCompletionPercentage(Integer earned, Integer required) {
        if (required == null || required <= 0 || earned == null || earned < 0) {
            return 0.0;
        }
        double rawPercentage = ((double) earned / required) * 100.0;
        return BigDecimal.valueOf(rawPercentage)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private CourseResponse toCourseResponse(Course course) {
        if (course == null) {
            return null;
        }
        return CourseResponse.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .department(course.getDepartment())
                .credits(course.getCredits())
                .semester(course.getSemester())
                .prerequisiteCourseIds(course.getPrerequisiteCourseIds())
                .elective(course.getElective())
                .active(course.getActive())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
