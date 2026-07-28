package com.campusguide.campus.academic.semesterplanner.service;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ConflictException;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.campus.academic.course.dto.CourseResponse;
import com.campusguide.campus.academic.course.service.CourseService;
import com.campusguide.campus.academic.progress.entity.StudentProgress;
import com.campusguide.campus.academic.progress.repository.StudentProgressRepository;
import com.campusguide.campus.academic.roadmap.service.RoadmapService;
import com.campusguide.campus.academic.semesterplanner.dto.*;
import com.campusguide.campus.academic.semesterplanner.entity.SemesterPlan;
import com.campusguide.campus.academic.semesterplanner.repository.SemesterPlanRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.service.CurrentUserService;
import com.campusguide.personal.notification.service.interfaces.NotificationService;
import com.campusguide.personal.notification.enums.NotificationType;
import com.campusguide.personal.notification.enums.NotificationPriority;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SemesterPlanService {

    private final SemesterPlanRepository semesterPlanRepository;
    private final CurrentUserService currentUserService;
    private final CourseService courseService;
    private final RoadmapService roadmapService;
    private final StudentProgressRepository studentProgressRepository;
    private final NotificationService notificationService;


    /**
     * Creates a semester plan for the authenticated student.
     * Enforces one plan per student per semester.
     *
     * @param userDetails the authenticated user details
     * @param request the create plan request
     * @return the created SemesterPlanResponse
     */
    public SemesterPlanResponse createSemesterPlan(UserDetails userDetails, CreateSemesterPlanRequest request) {
        User user = currentUserService.getCurrentUser(userDetails);

        // Validate roadmap exists
        roadmapService.getRoadmapById(request.getRoadmapId());

        // Validate progress record exists
        studentProgressRepository.findByStudentId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student progress record not found for student ID: " + user.getId()));

        // Reject duplicate semester plans for the same student and semester
        if (semesterPlanRepository.findByStudentIdAndSemesterNumber(user.getId(), request.getSemesterNumber()).isPresent()) {
            throw new ConflictException("Semester plan already exists for this student in semester " + request.getSemesterNumber());
        }

        SemesterPlan plan = SemesterPlan.builder()
                .studentId(user.getId())
                .roadmapId(request.getRoadmapId())
                .semesterNumber(request.getSemesterNumber())
                .plannedCourseIds(new ArrayList<>())
                .totalPlannedCredits(0)
                .finalized(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        plan = semesterPlanRepository.save(plan);
        return toSemesterPlanResponse(plan);
    }

    /**
     * Updates an existing semester plan.
     * Supports partial updates.
     * Only owner or SUPER_ADMIN.
     * Reject updates once finalized.
     *
     * @param userDetails the authenticated user details
     * @param planId the ID of the plan to update
     * @param request the update request
     * @return the updated SemesterPlanResponse
     */
    public SemesterPlanResponse updateSemesterPlan(UserDetails userDetails, String planId, UpdateSemesterPlanRequest request) {
        User user = currentUserService.getCurrentUser(userDetails);

        SemesterPlan plan = semesterPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester plan not found with ID: " + planId));

        // Authorization check
        checkOwnerOrAdmin(plan, user);

        // Reject updates once finalized
        if (Boolean.TRUE.equals(plan.getFinalized())) {
            throw new BadRequestException("A finalized semester plan cannot be modified.");
        }

        // Support partial updates
        if (request.getRoadmapId() != null) {
            roadmapService.getRoadmapById(request.getRoadmapId());
            plan.setRoadmapId(request.getRoadmapId());
        }

        if (request.getSemesterNumber() != null) {
            // Check duplicate semester plans
            final String currentPlanId = plan.getId();
            semesterPlanRepository.findByStudentIdAndSemesterNumber(plan.getStudentId(), request.getSemesterNumber())
                    .ifPresent(existingPlan -> {
                        if (!existingPlan.getId().equals(currentPlanId)) {
                            throw new ConflictException("Semester plan already exists for this student in semester " + request.getSemesterNumber());
                        }
                    });
            plan.setSemesterNumber(request.getSemesterNumber());
        }

        if (request.getPlannedCourseIds() != null) {
            List<String> courseIds = request.getPlannedCourseIds().stream().distinct().toList();
            
            StudentProgress progress = studentProgressRepository.findByStudentId(plan.getStudentId())
                    .orElseThrow(() -> new BadRequestException("Student progress record not found"));
            List<String> completedCourseIds = progress.getCompletedCourseIds() != null
                    ? progress.getCompletedCourseIds()
                    : new ArrayList<>();

            int totalCredits = 0;
            for (String courseId : courseIds) {
                CourseResponse course = courseService.getCourseById(courseId);
                if (course == null || Boolean.FALSE.equals(course.getActive())) {
                    throw new BadRequestException("Course with ID " + courseId + " is inactive or not found");
                }
                
                List<String> prerequisites = course.getPrerequisiteCourseIds();
                if (prerequisites != null) {
                    for (String prereqId : prerequisites) {
                        if (!completedCourseIds.contains(prereqId)) {
                            throw new BadRequestException("Prerequisite course with ID " + prereqId + " has not been completed");
                        }
                    }
                }
                
                totalCredits += course.getCredits();
            }
            plan.setPlannedCourseIds(new ArrayList<>(courseIds));
            plan.setTotalPlannedCredits(totalCredits);
        }

        if (request.getFinalized() != null) {
            if (request.getFinalized()) {
                if (plan.getPlannedCourseIds() == null || plan.getPlannedCourseIds().isEmpty()) {
                    throw new BadRequestException("Cannot finalize an empty plan");
                }
                plan.setFinalized(true);
            } else {
                plan.setFinalized(false);
            }
        }

        plan.setUpdatedAt(Instant.now());
        plan = semesterPlanRepository.save(plan);
        return toSemesterPlanResponse(plan);
    }

    /**
     * Adds a course to the plan, validating prerequisites against StudentProgress.
     *
     * @param userDetails the authenticated user details
     * @param planId the ID of the plan
     * @param courseId the ID of the course to add
     * @return the updated SemesterPlanResponse
     */
    public SemesterPlanResponse addCourse(UserDetails userDetails, String planId, String courseId) {
        User user = currentUserService.getCurrentUser(userDetails);

        SemesterPlan plan = semesterPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester plan not found with ID: " + planId));

        // Authorization check
        checkOwnerOrAdmin(plan, user);

        // Reject updates once finalized
        if (Boolean.TRUE.equals(plan.getFinalized())) {
            throw new BadRequestException("A finalized semester plan cannot be modified.");
        }

        // Validate course exists (throws ResourceNotFoundException if course is inactive)
        CourseResponse course = courseService.getCourseById(courseId);

        // Reject duplicate planned courses
        if (plan.getPlannedCourseIds() != null && plan.getPlannedCourseIds().contains(courseId)) {
            throw new ConflictException("Course is already planned in this semester plan");
        }

        if (plan.getPlannedCourseIds() == null) {
            plan.setPlannedCourseIds(new ArrayList<>());
        }
        plan.getPlannedCourseIds().add(courseId);
        
        // Validate every prerequisite has been completed using StudentProgress
        StudentProgress progress = studentProgressRepository.findByStudentId(plan.getStudentId())
                .orElseThrow(() -> new BadRequestException("Student progress record not found"));

        List<String> completedCourseIds = progress.getCompletedCourseIds();
        if (completedCourseIds == null) {
            completedCourseIds = new ArrayList<>();
        }

        List<String> prerequisites = course.getPrerequisiteCourseIds();
        if (prerequisites != null) {
            for (String prereqId : prerequisites) {
                if (!completedCourseIds.contains(prereqId)) {
                    throw new BadRequestException("Prerequisite course with ID " + prereqId + " has not been completed");
                }
            }
        }

        // Increase totalPlannedCredits
        int courseCredits = course.getCredits() != null ? course.getCredits() : 0;
        plan.setTotalPlannedCredits(plan.getTotalPlannedCredits() + courseCredits);

        plan.setUpdatedAt(Instant.now());
        plan = semesterPlanRepository.save(plan);
        return toSemesterPlanResponse(plan);
    }

    /**
     * Removes a course from the plan, deducting credits.
     *
     * @param userDetails the authenticated user details
     * @param planId the ID of the plan
     * @param courseId the ID of the course to remove
     * @return the updated SemesterPlanResponse
     */
    public SemesterPlanResponse removeCourse(UserDetails userDetails, String planId, String courseId) {
        User user = currentUserService.getCurrentUser(userDetails);

        SemesterPlan plan = semesterPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester plan not found with ID: " + planId));

        // Authorization check
        checkOwnerOrAdmin(plan, user);

        // Reject updates once finalized
        if (Boolean.TRUE.equals(plan.getFinalized())) {
            throw new BadRequestException("A finalized semester plan cannot be modified.");
        }

        // Reject missing course
        if (plan.getPlannedCourseIds() == null || !plan.getPlannedCourseIds().contains(courseId)) {
            throw new BadRequestException("Course is not in the semester plan");
        }

        // Validate course exists in catalog to get its credits
        CourseResponse course = courseService.getCourseById(courseId);

        // Remove course
        plan.getPlannedCourseIds().remove(courseId);

        // Deduct credits, never allow negative credits
        int courseCredits = course.getCredits() != null ? course.getCredits() : 0;
        int newCredits = plan.getTotalPlannedCredits() - courseCredits;
        plan.setTotalPlannedCredits(Math.max(0, newCredits));

        plan.setUpdatedAt(Instant.now());
        plan = semesterPlanRepository.save(plan);
        return toSemesterPlanResponse(plan);
    }

    /**
     * Finalizes the semester plan.
     *
     * @param userDetails the authenticated user details
     * @param planId the ID of the plan to finalize
     * @return the updated SemesterPlanResponse
     */
    public SemesterPlanResponse finalizeSemesterPlan(UserDetails userDetails, String planId) {
        User user = currentUserService.getCurrentUser(userDetails);

        SemesterPlan plan = semesterPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester plan not found with ID: " + planId));

        // Authorization check
        checkOwnerOrAdmin(plan, user);

        // Reject empty plans
        if (plan.getPlannedCourseIds() == null || plan.getPlannedCourseIds().isEmpty()) {
            throw new BadRequestException("Cannot finalize an empty semester plan");
        }

        // Mark finalized = true
        plan.setFinalized(true);

        plan.setUpdatedAt(Instant.now());
        plan = semesterPlanRepository.save(plan);
        notificationService.createNotification(
                user.getId(),
                "Semester Plan Finalized",
                "Your plan for Semester " + plan.getSemesterNumber() + " has been successfully finalized.",
                NotificationType.ACADEMIC,
                NotificationPriority.HIGH,
                null
        );
        return toSemesterPlanResponse(plan);
    }

    public void deletePlan(UserDetails userDetails, String planId) {
        User user = currentUserService.getCurrentUser(userDetails);

        SemesterPlan plan = semesterPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester plan not found with ID: " + planId));

        // Authorization check
        checkOwnerOrAdmin(plan, user);

        semesterPlanRepository.delete(plan);
    }

    /**
     * Gets all semester plans for the authenticated student.
     *
     * @param userDetails the authenticated user details
     * @return list of plans
     */
    public List<SemesterPlanResponse> getMyPlans(UserDetails userDetails) {
        User user = currentUserService.getCurrentUser(userDetails);

        return semesterPlanRepository.findByStudentIdOrderBySemesterNumberAsc(user.getId()).stream()
                .map(this::toSemesterPlanResponse)
                .toList();
    }

    /**
     * Gets a specific semester plan.
     *
     * @param userDetails the authenticated user details
     * @param planId the ID of the plan
     * @return the plan
     */
    public SemesterPlanResponse getSemesterPlan(UserDetails userDetails, String planId) {
        User user = currentUserService.getCurrentUser(userDetails);

        SemesterPlan plan = semesterPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester plan not found with ID: " + planId));

        // Authorization check
        checkOwnerOrAdmin(plan, user);

        return toSemesterPlanResponse(plan);
    }

    /**
     * Gets plans of a specific student (SUPER_ADMIN only).
     *
     * @param userDetails the authenticated user details
     * @param studentId the ID of the student
     * @return list of plans
     */
    public List<SemesterPlanResponse> getPlansByStudent(UserDetails userDetails, String studentId) {
        User user = currentUserService.getCurrentUser(userDetails);

        // Authorization check
        if (user.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Only SUPER_ADMIN can view other students' plans");
        }

        return semesterPlanRepository.findByStudentIdOrderBySemesterNumberAsc(studentId).stream()
                .map(this::toSemesterPlanResponse)
                .toList();
    }

    // --- PRIVATE UTILITY AND MAPPER METHODS ---

    private void checkOwnerOrAdmin(SemesterPlan plan, User user) {
        if (!plan.getStudentId().equals(user.getId()) && user.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("You are not authorized to access/modify this semester plan");
        }
    }

    private SemesterPlanResponse toSemesterPlanResponse(SemesterPlan plan) {
        if (plan == null) {
            return null;
        }
        return SemesterPlanResponse.builder()
                .id(plan.getId())
                .studentId(plan.getStudentId())
                .roadmapId(plan.getRoadmapId())
                .semesterNumber(plan.getSemesterNumber())
                .plannedCourseIds(plan.getPlannedCourseIds() != null
                        ? new ArrayList<>(plan.getPlannedCourseIds())
                        : new ArrayList<>())
                .totalPlannedCredits(plan.getTotalPlannedCredits())
                .finalized(plan.getFinalized())
                .createdAt(plan.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(plan.getCreatedAt(), java.time.ZoneId.systemDefault()) : null)
                .updatedAt(plan.getUpdatedAt() != null ? java.time.LocalDateTime.ofInstant(plan.getUpdatedAt(), java.time.ZoneId.systemDefault()) : null)
                .build();
    }

    private SemesterPlanSummaryResponse toSemesterPlanSummaryResponse(SemesterPlan plan) {
        if (plan == null) {
            return null;
        }
        return SemesterPlanSummaryResponse.builder()
                .id(plan.getId())
                .studentId(plan.getStudentId())
                .roadmapId(plan.getRoadmapId())
                .semesterNumber(plan.getSemesterNumber())
                .totalPlannedCredits(plan.getTotalPlannedCredits())
                .finalized(plan.getFinalized())
                .build();
    }
}
