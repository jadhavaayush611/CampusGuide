package com.campusguide.modules.progress.service;

import com.campusguide.exception.BadRequestException;
import com.campusguide.exception.ConflictException;
import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.exception.UnauthorisedException;
import com.campusguide.modules.course.dto.CourseResponse;
import com.campusguide.modules.course.service.CourseService;
import com.campusguide.modules.progress.dto.*;
import com.campusguide.modules.progress.entity.StudentProgress;
import com.campusguide.modules.progress.repository.StudentProgressRepository;
import com.campusguide.modules.roadmap.service.RoadmapService;
import com.campusguide.modules.user.entity.Role;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentProgressService {

    private final StudentProgressRepository studentProgressRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final RoadmapService roadmapService;

    /**
     * Creates a student progress record for the authenticated student.
     * Enforces one progress record per student.
     *
     * @param userDetails the authenticated user details
     * @param request the create progress request
     * @return the created StudentProgressResponse
     */
    public StudentProgressResponse createProgress(UserDetails userDetails, CreateStudentProgressRequest request) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        // Enforce one progress record per student
        if (studentProgressRepository.findByStudentId(user.getId()).isPresent()) {
            throw new ConflictException("Student progress already exists for this student");
        }

        // Validate roadmap exists
        roadmapService.getRoadmapById(request.getRoadmapId());

        StudentProgress progress = StudentProgress.builder()
                .studentId(user.getId())
                .roadmapId(request.getRoadmapId())
                .completedCourseIds(new ArrayList<>())
                .currentSemester(1)
                .totalCreditsEarned(0)
                .currentGpa(0.0)
                .graduationEligible(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        progress = studentProgressRepository.save(progress);
        return toStudentProgressResponse(progress);
    }

    /**
     * Updates an existing student progress record.
     * Allows updates by the owner or a SUPER_ADMIN.
     * Supports partial updates.
     *
     * @param userDetails the authenticated user details
     * @param request the update progress request
     * @return the updated StudentProgressResponse
     */
    public StudentProgressResponse updateProgress(UserDetails userDetails, UpdateStudentProgressRequest request) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        // Determine target student ID to update (admins can specify target studentId in body)
        String targetStudentId = request.getStudentId() != null ? request.getStudentId() : user.getId();

        StudentProgress progress = studentProgressRepository.findByStudentId(targetStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student progress not found for student: " + targetStudentId));

        // Check authorization: Owner or SUPER_ADMIN only
        if (!progress.getStudentId().equals(user.getId()) && user.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("You are not authorized to update this student progress");
        }

        // Partial updates for permitted fields
        if (request.getRoadmapId() != null) {
            roadmapService.getRoadmapById(request.getRoadmapId());
            progress.setRoadmapId(request.getRoadmapId());
        }

        if (request.getCurrentSemester() != null) {
            if (request.getCurrentSemester() <= 0) {
                throw new BadRequestException("Semester must be greater than 0");
            }
            progress.setCurrentSemester(request.getCurrentSemester());
        }

        // Recalculate server-derived values (total credits and graduation eligibility)
        progress.setTotalCreditsEarned(calculateTotalCredits(progress));
        recalculateGraduationEligibility(progress);

        progress.setUpdatedAt(LocalDateTime.now());
        progress = studentProgressRepository.save(progress);
        return toStudentProgressResponse(progress);
    }

    /**
     * Performs administrative academic updates on a student progress record.
     * Accessible by SUPER_ADMIN only.
     *
     * @param userDetails the authenticated user details
     * @param request the admin update progress request
     * @return the updated StudentProgressResponse
     */
    public StudentProgressResponse adminUpdateProgress(UserDetails userDetails, AdminUpdateStudentProgressRequest request) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        if (user.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Only SUPER_ADMIN is authorized to perform administrative academic updates");
        }

        String targetStudentId = request.getStudentId();
        if (targetStudentId == null) {
            throw new BadRequestException("Student ID must be specified for administrative updates");
        }

        StudentProgress progress = studentProgressRepository.findByStudentId(targetStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student progress not found for student: " + targetStudentId));

        if (request.getRoadmapId() != null) {
            roadmapService.getRoadmapById(request.getRoadmapId());
            progress.setRoadmapId(request.getRoadmapId());
        }

        if (request.getCurrentSemester() != null) {
            if (request.getCurrentSemester() <= 0) {
                throw new BadRequestException("Semester must be greater than 0");
            }
            progress.setCurrentSemester(request.getCurrentSemester());
        }

        // MVP: SUPER_ADMIN updates GPA until grading system exists.
        // Future migration path: GPA will be calculated directly from course grades.
        if (request.getCurrentGpa() != null) {
            if (request.getCurrentGpa() < 0.0 || request.getCurrentGpa() > 10.0) {
                throw new BadRequestException("GPA must be between 0.0 and 10.0");
            }
            progress.setCurrentGpa(request.getCurrentGpa());
        }

        // Recalculate server-derived values (total credits and graduation eligibility)
        progress.setTotalCreditsEarned(calculateTotalCredits(progress));
        recalculateGraduationEligibility(progress);

        progress.setUpdatedAt(LocalDateTime.now());
        progress = studentProgressRepository.save(progress);
        return toStudentProgressResponse(progress);
    }

    /**
     * Calculates the total credits earned by a student based on completed courses.
     *
     * @param progress the student progress record
     * @return the total sum of credits for all completed courses
     */
    private int calculateTotalCredits(StudentProgress progress) {
        if (progress.getCompletedCourseIds() == null || progress.getCompletedCourseIds().isEmpty()) {
            return 0;
        }
        int totalCredits = 0;
        for (String courseId : progress.getCompletedCourseIds()) {
            try {
                CourseResponse course = courseService.getCourseByIdInternal(courseId);
                if (course != null && course.getCredits() != null) {
                    totalCredits += course.getCredits();
                }
            } catch (Exception e) {
                // Ignore if course not found
            }
        }
        return totalCredits;
    }

    /**
     * Marks a course completed, adding its credits to the student's total credits.
     * Enforces that courses cannot be marked completed twice.
     *
     * @param userDetails the authenticated user details
     * @param courseId the course ID to mark completed
     * @param targetStudentId optional target student ID (for SUPER_ADMIN updates)
     * @return the updated StudentProgressResponse
     */
    public StudentProgressResponse markCourseCompleted(UserDetails userDetails, String courseId, String targetStudentId) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        String studentId = targetStudentId != null ? targetStudentId : user.getId();

        // Check authorization: Owner or SUPER_ADMIN
        if (!studentId.equals(user.getId()) && user.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("You are not authorized to modify this student progress");
        }

        StudentProgress progress = studentProgressRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student progress not found for student: " + studentId));

        // Validate course exists (throws ResourceNotFoundException if course not found or inactive)
        CourseResponse course = courseService.getCourseById(courseId);

        // Reject duplicate completion
        if (progress.getCompletedCourseIds() != null && progress.getCompletedCourseIds().contains(courseId)) {
            throw new ConflictException("Course is already marked as completed");
        }

        if (progress.getCompletedCourseIds() == null) {
            progress.setCompletedCourseIds(new ArrayList<>());
        }

        progress.getCompletedCourseIds().add(courseId);

        // Recalculate total credits and graduation eligibility
        progress.setTotalCreditsEarned(calculateTotalCredits(progress));
        recalculateGraduationEligibility(progress);
        progress.setUpdatedAt(LocalDateTime.now());

        progress = studentProgressRepository.save(progress);
        return toStudentProgressResponse(progress);
    }

    /**
     * Removes a course from the completed courses list, deducting its credits.
     * Never allows total credits to become negative.
     *
     * @param userDetails the authenticated user details
     * @param courseId the course ID to remove
     * @param targetStudentId optional target student ID (for SUPER_ADMIN updates)
     * @return the updated StudentProgressResponse
     */
    public StudentProgressResponse removeCompletedCourse(UserDetails userDetails, String courseId, String targetStudentId) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        String studentId = targetStudentId != null ? targetStudentId : user.getId();

        // Check authorization: Owner or SUPER_ADMIN
        if (!studentId.equals(user.getId()) && user.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("You are not authorized to modify this student progress");
        }

        StudentProgress progress = studentProgressRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student progress not found for student: " + studentId));

        // Reject missing completion
        if (progress.getCompletedCourseIds() == null || !progress.getCompletedCourseIds().contains(courseId)) {
            throw new BadRequestException("Course is not marked as completed");
        }

        // Validate course exists to get its credits
        CourseResponse course = courseService.getCourseByIdInternal(courseId);

        progress.getCompletedCourseIds().remove(courseId);

        // Recalculate total credits and graduation eligibility
        progress.setTotalCreditsEarned(calculateTotalCredits(progress));
        recalculateGraduationEligibility(progress);
        progress.setUpdatedAt(LocalDateTime.now());

        progress = studentProgressRepository.save(progress);
        return toStudentProgressResponse(progress);
    }

    /**
     * Gets the student progress for the authenticated student.
     *
     * @param userDetails the authenticated user details
     * @return the StudentProgressResponse
     */
    public StudentProgressResponse getProgress(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        StudentProgress progress = studentProgressRepository.findByStudentId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student progress not found for student: " + user.getId()));

        return toStudentProgressResponse(progress);
    }

    /**
     * Gets progress record by student ID (SUPER_ADMIN only).
     *
     * @param userDetails the authenticated user details
     * @param studentId the student's ID
     * @return the StudentProgressResponse
     */
    public StudentProgressResponse getProgressByStudent(UserDetails userDetails, String studentId) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        if (user.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Only SUPER_ADMIN can access another student's progress");
        }

        StudentProgress progress = studentProgressRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student progress not found for student ID: " + studentId));

        return toStudentProgressResponse(progress);
    }

    /**
     * Gets all student progress records ordered by createdAt descending (SUPER_ADMIN only).
     *
     * @param userDetails the authenticated user details
     * @return list of StudentProgressResponse
     */
    public List<StudentProgressResponse> getAllProgress(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));

        if (user.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Only SUPER_ADMIN can access all progress records");
        }

        return studentProgressRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toStudentProgressResponse)
                .toList();
    }

    private void recalculateGraduationEligibility(StudentProgress progress) {
        if (progress.getRoadmapId() != null) {
            try {
                com.campusguide.modules.roadmap.dto.RoadmapResponse roadmap = roadmapService.getRoadmapById(progress.getRoadmapId());
                if (roadmap != null && roadmap.getTotalCredits() != null) {
                    progress.setGraduationEligible(progress.getTotalCreditsEarned() >= roadmap.getTotalCredits());
                }
            } catch (Exception e) {
                // Ignore if roadmap not found or other errors
            }
        }
    }

    // --- PRIVATE MAPPER METHODS ---

    private StudentProgressResponse toStudentProgressResponse(StudentProgress progress) {
        if (progress == null) {
            return null;
        }
        return StudentProgressResponse.builder()
                .id(progress.getId())
                .studentId(progress.getStudentId())
                .roadmapId(progress.getRoadmapId())
                .completedCourseIds(progress.getCompletedCourseIds() != null 
                        ? new ArrayList<>(progress.getCompletedCourseIds()) 
                        : new ArrayList<>())
                .currentSemester(progress.getCurrentSemester())
                .totalCreditsEarned(progress.getTotalCreditsEarned())
                .currentGpa(progress.getCurrentGpa())
                .graduationEligible(progress.getGraduationEligible())
                .createdAt(progress.getCreatedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }

    private StudentProgressSummaryResponse toStudentProgressSummaryResponse(StudentProgress progress) {
        if (progress == null) {
            return null;
        }
        return StudentProgressSummaryResponse.builder()
                .id(progress.getId())
                .studentId(progress.getStudentId())
                .roadmapId(progress.getRoadmapId())
                .currentSemester(progress.getCurrentSemester())
                .totalCreditsEarned(progress.getTotalCreditsEarned())
                .currentGpa(progress.getCurrentGpa())
                .graduationEligible(progress.getGraduationEligible())
                .build();
    }
}
