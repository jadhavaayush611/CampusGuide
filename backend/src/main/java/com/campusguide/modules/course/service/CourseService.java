package com.campusguide.modules.course.service;

import com.campusguide.exception.BadRequestException;
import com.campusguide.exception.ConflictException;
import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.modules.course.dto.CreateCourseRequest;
import com.campusguide.modules.course.dto.CourseResponse;
import com.campusguide.modules.course.dto.CourseSummaryResponse;
import com.campusguide.modules.course.dto.UpdateCourseRequest;
import com.campusguide.modules.course.entity.Course;
import com.campusguide.modules.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    /**
     * Creates a new course in the catalog.
     *
     * @param request the request containing details of the course to create
     * @return the created course details
     * @throws ConflictException if a course with the same courseCode already exists
     * @throws BadRequestException if credits or semester is not positive, or if prerequisite IDs are invalid
     */
    public CourseResponse createCourse(CreateCourseRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new ConflictException("Course with code '" + request.getCourseCode() + "' already exists");
        }

        if (request.getCredits() == null || request.getCredits() <= 0) {
            throw new BadRequestException("Credits must be greater than 0");
        }

        if (request.getSemester() == null || request.getSemester() <= 0) {
            throw new BadRequestException("Semester must be greater than 0");
        }

        List<String> prereqIds = request.getPrerequisiteCourseIds() != null
                ? request.getPrerequisiteCourseIds().stream().distinct().toList()
                : new ArrayList<>();

        if (!prereqIds.isEmpty()) {
            List<Course> prereqs = courseRepository.findAllById(prereqIds);
            if (prereqs.size() != prereqIds.size()) {
                List<String> foundIds = prereqs.stream().map(Course::getId).toList();
                for (String prereqId : prereqIds) {
                    if (!foundIds.contains(prereqId)) {
                        throw new BadRequestException("Prerequisite course with ID " + prereqId + " does not exist");
                    }
                }
            }
            for (Course prereq : prereqs) {
                if (Boolean.FALSE.equals(prereq.getActive())) {
                    throw new BadRequestException("Prerequisite course with ID " + prereq.getId() + " is inactive");
                }
            }
        }

        Course course = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .department(request.getDepartment())
                .credits(request.getCredits())
                .semester(request.getSemester())
                .prerequisiteCourseIds(prereqIds)
                .elective(request.getElective() != null ? request.getElective() : false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        course = courseRepository.save(course);
        return toCourseResponse(course);
    }

    /**
     * Updates an existing course's fields. Supports partial updates.
     *
     * @param courseId the ID of the course to update
     * @param request the request containing updated fields
     * @return the updated course details
     * @throws ResourceNotFoundException if the course with the specified ID does not exist or is inactive
     * @throws ConflictException if the updated courseCode conflicts with an existing course
     * @throws BadRequestException if credits or semester is not positive, or if prerequisite IDs are invalid
     */
    public CourseResponse updateCourse(String courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (Boolean.FALSE.equals(course.getActive())) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        if (request.getCourseCode() != null && !request.getCourseCode().equals(course.getCourseCode())) {
            if (courseRepository.existsByCourseCode(request.getCourseCode())) {
                throw new ConflictException("Course with code '" + request.getCourseCode() + "' already exists");
            }
            course.setCourseCode(request.getCourseCode());
        }

        if (request.getCourseName() != null) {
            course.setCourseName(request.getCourseName());
        }

        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }

        if (request.getDepartment() != null) {
            course.setDepartment(request.getDepartment());
        }

        if (request.getCredits() != null) {
            if (request.getCredits() <= 0) {
                throw new BadRequestException("Credits must be greater than 0");
            }
            course.setCredits(request.getCredits());
        }

        if (request.getSemester() != null) {
            if (request.getSemester() <= 0) {
                throw new BadRequestException("Semester must be greater than 0");
            }
            course.setSemester(request.getSemester());
        }

        if (request.getPrerequisiteCourseIds() != null) {
            List<String> prereqIds = request.getPrerequisiteCourseIds().stream().distinct().toList();
            if (prereqIds.contains(courseId)) {
                throw new BadRequestException("A course cannot have itself as a prerequisite");
            }
            if (!prereqIds.isEmpty()) {
                List<Course> prereqs = courseRepository.findAllById(prereqIds);
                if (prereqs.size() != prereqIds.size()) {
                    List<String> foundIds = prereqs.stream().map(Course::getId).toList();
                    for (String prereqId : prereqIds) {
                        if (!foundIds.contains(prereqId)) {
                            throw new BadRequestException("Prerequisite course with ID " + prereqId + " does not exist");
                        }
                    }
                }
                for (Course prereq : prereqs) {
                    if (Boolean.FALSE.equals(prereq.getActive())) {
                        throw new BadRequestException("Prerequisite course with ID " + prereq.getId() + " is inactive");
                    }
                }
            }
            course.setPrerequisiteCourseIds(prereqIds);
        }

        if (request.getElective() != null) {
            course.setElective(request.getElective());
        }

        if (request.getActive() != null) {
            course.setActive(request.getActive());
        }

        course.setUpdatedAt(LocalDateTime.now());
        course = courseRepository.save(course);
        return toCourseResponse(course);
    }

    /**
     * Soft deletes a course by setting active to false.
     *
     * @param courseId the ID of the course to soft delete
     * @throws ResourceNotFoundException if the course is not found or is already inactive
     */
    public void deleteCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (Boolean.FALSE.equals(course.getActive())) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        course.setActive(false);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    /**
     * Retrieves all active courses ordered by courseCode ascending.
     *
     * @return a list of summaries of all active courses
     */
    public List<CourseSummaryResponse> getAllCourses() {
        return courseRepository.findByActiveTrueOrderByCourseCodeAsc().stream()
                .map(this::toCourseSummaryResponse)
                .toList();
    }

    /**
     * Retrieves an active course by its ID.
     *
     * @param courseId the ID of the course to retrieve
     * @return the course details
     * @throws ResourceNotFoundException if the course does not exist or is inactive
     */
    public CourseResponse getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (Boolean.FALSE.equals(course.getActive())) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return toCourseResponse(course);
    }

    /**
     * Retrieves a course by its ID internally, regardless of whether it is active.
     *
     * @param courseId the ID of the course to retrieve
     * @return the course details
     * @throws ResourceNotFoundException if the course does not exist
     */
    public CourseResponse getCourseByIdInternal(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        return toCourseResponse(course);
    }

    /**
     * Retrieves active courses in a specific department ordered by courseCode ascending.
     *
     * @param department the department to filter by
     * @return a list of summaries of active courses in the department
     */
    public List<CourseSummaryResponse> getByDepartment(String department) {
        return courseRepository.findByDepartmentAndActiveTrueOrderByCourseCodeAsc(department).stream()
                .map(this::toCourseSummaryResponse)
                .toList();
    }

    /**
     * Retrieves active courses offered in a specific semester ordered by courseCode ascending.
     *
     * @param semester the semester to filter by
     * @return a list of summaries of active courses in the semester
     */
    public List<CourseSummaryResponse> getBySemester(Integer semester) {
        if (semester == null || semester <= 0) {
            throw new BadRequestException("Semester must be greater than 0");
        }
        return courseRepository.findBySemesterAndActiveTrueOrderByCourseCodeAsc(semester).stream()
                .map(this::toCourseSummaryResponse)
                .toList();
    }

    /**
     * Retrieves active elective courses ordered by courseCode ascending.
     *
     * @return a list of summaries of active elective courses
     */
    public List<CourseSummaryResponse> getElectives() {
        return courseRepository.findByElectiveTrueAndActiveTrueOrderByCourseCodeAsc().stream()
                .map(this::toCourseSummaryResponse)
                .toList();
    }

    /**
     * Retrieves active mandatory (non-elective) courses ordered by courseCode ascending.
     *
     * @return a list of summaries of active mandatory courses
     */
    public List<CourseSummaryResponse> getMandatoryCourses() {
        return courseRepository.findByElectiveFalseAndActiveTrueOrderByCourseCodeAsc().stream()
                .map(this::toCourseSummaryResponse)
                .toList();
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

    private CourseSummaryResponse toCourseSummaryResponse(Course course) {
        if (course == null) {
            return null;
        }
        return CourseSummaryResponse.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .department(course.getDepartment())
                .credits(course.getCredits())
                .semester(course.getSemester())
                .elective(course.getElective())
                .build();
    }
}
