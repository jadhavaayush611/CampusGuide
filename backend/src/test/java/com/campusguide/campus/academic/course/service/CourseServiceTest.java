package com.campusguide.campus.academic.course.service;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ConflictException;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.campus.academic.course.dto.CreateCourseRequest;
import com.campusguide.campus.academic.course.dto.CourseResponse;
import com.campusguide.campus.academic.course.dto.CourseSummaryResponse;
import com.campusguide.campus.academic.course.dto.UpdateCourseRequest;
import com.campusguide.campus.academic.course.entity.Course;
import com.campusguide.campus.academic.course.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    private CreateCourseRequest createRequest;
    private UpdateCourseRequest updateRequest;
    private Course course;
    private Course prereqCourse;

    @BeforeEach
    void setUp() {
        createRequest = CreateCourseRequest.builder()
                .courseCode("CS101")
                .courseName("Introduction to Computer Science")
                .description("Basics of computer programming and computer systems.")
                .department("CS")
                .credits(4)
                .semester(1)
                .prerequisiteCourseIds(new ArrayList<>())
                .elective(false)
                .build();

        updateRequest = UpdateCourseRequest.builder()
                .courseName("Intro to Computer Science")
                .credits(3)
                .semester(2)
                .elective(true)
                .build();

        course = Course.builder()
                .id("course-123")
                .courseCode("CS101")
                .courseName("Introduction to Computer Science")
                .description("Basics of computer programming and computer systems.")
                .department("CS")
                .credits(4)
                .semester(1)
                .prerequisiteCourseIds(new ArrayList<>())
                .elective(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        prereqCourse = Course.builder()
                .id("prereq-456")
                .courseCode("MATH101")
                .courseName("Calculus I")
                .department("MATH")
                .credits(4)
                .semester(1)
                .prerequisiteCourseIds(new ArrayList<>())
                .elective(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createCourse_Successful() {
        when(courseRepository.existsByCourseCode("CS101")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.createCourse(createRequest);

        assertNotNull(response);
        assertEquals("course-123", response.getId());
        assertEquals("CS101", response.getCourseCode());
        assertEquals("Introduction to Computer Science", response.getCourseName());
        assertEquals("CS", response.getDepartment());
        assertEquals(4, response.getCredits());
        assertEquals(1, response.getSemester());
        assertTrue(response.getActive());
        assertFalse(response.getElective());

        verify(courseRepository).existsByCourseCode("CS101");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_WithPrerequisites_Successful() {
        createRequest.setPrerequisiteCourseIds(List.of("prereq-456"));
        course.setPrerequisiteCourseIds(List.of("prereq-456"));

        when(courseRepository.existsByCourseCode("CS101")).thenReturn(false);
        when(courseRepository.findAllById(List.of("prereq-456"))).thenReturn(List.of(prereqCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.createCourse(createRequest);

        assertNotNull(response);
        assertEquals(1, response.getPrerequisiteCourseIds().size());
        assertEquals("prereq-456", response.getPrerequisiteCourseIds().get(0));

        verify(courseRepository).existsByCourseCode("CS101");
        verify(courseRepository).findAllById(List.of("prereq-456"));
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_DuplicateCourseCode_ThrowsConflictException() {
        when(courseRepository.existsByCourseCode("CS101")).thenReturn(true);

        assertThrows(ConflictException.class, () -> courseService.createCourse(createRequest));

        verify(courseRepository).existsByCourseCode("CS101");
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void createCourse_InvalidPrerequisite_ThrowsBadRequestException() {
        createRequest.setPrerequisiteCourseIds(List.of("invalid-id"));
        when(courseRepository.existsByCourseCode("CS101")).thenReturn(false);
        when(courseRepository.findAllById(List.of("invalid-id"))).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> courseService.createCourse(createRequest));

        verify(courseRepository).existsByCourseCode("CS101");
        verify(courseRepository).findAllById(List.of("invalid-id"));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void createCourse_InactivePrerequisite_ThrowsBadRequestException() {
        prereqCourse.setActive(false);
        createRequest.setPrerequisiteCourseIds(List.of("prereq-456"));

        when(courseRepository.existsByCourseCode("CS101")).thenReturn(false);
        when(courseRepository.findAllById(List.of("prereq-456"))).thenReturn(List.of(prereqCourse));

        assertThrows(BadRequestException.class, () -> courseService.createCourse(createRequest));

        verify(courseRepository).existsByCourseCode("CS101");
        verify(courseRepository).findAllById(List.of("prereq-456"));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void createCourse_InvalidCredits_ThrowsBadRequestException() {
        createRequest.setCredits(0);
        assertThrows(BadRequestException.class, () -> courseService.createCourse(createRequest));

        createRequest.setCredits(-2);
        assertThrows(BadRequestException.class, () -> courseService.createCourse(createRequest));
    }

    @Test
    void createCourse_InvalidSemester_ThrowsBadRequestException() {
        createRequest.setSemester(0);
        assertThrows(BadRequestException.class, () -> courseService.createCourse(createRequest));

        createRequest.setSemester(-1);
        assertThrows(BadRequestException.class, () -> courseService.createCourse(createRequest));
    }

    @Test
    void updateCourse_Successful() {
        when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse response = courseService.updateCourse("course-123", updateRequest);

        assertNotNull(response);
        assertEquals("Intro to Computer Science", response.getCourseName());
        assertEquals(3, response.getCredits());
        assertEquals(2, response.getSemester());
        assertTrue(response.getElective());

        verify(courseRepository).findById("course-123");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void updateCourse_WithValidPrerequisites_Successful() {
        updateRequest.setPrerequisiteCourseIds(List.of("prereq-456"));
        when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));
        when(courseRepository.findAllById(List.of("prereq-456"))).thenReturn(List.of(prereqCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse response = courseService.updateCourse("course-123", updateRequest);

        assertNotNull(response);
        assertEquals(1, response.getPrerequisiteCourseIds().size());
        assertEquals("prereq-456", response.getPrerequisiteCourseIds().get(0));

        verify(courseRepository).findById("course-123");
        verify(courseRepository).findAllById(List.of("prereq-456"));
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void updateCourse_SelfPrerequisite_ThrowsBadRequestException() {
        updateRequest.setPrerequisiteCourseIds(List.of("course-123"));
        when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));

        assertThrows(BadRequestException.class, () -> courseService.updateCourse("course-123", updateRequest));

        verify(courseRepository).findById("course-123");
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateCourse_DuplicateCourseCode_ThrowsConflictException() {
        updateRequest.setCourseCode("MATH101");
        when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));
        when(courseRepository.existsByCourseCode("MATH101")).thenReturn(true);

        assertThrows(ConflictException.class, () -> courseService.updateCourse("course-123", updateRequest));

        verify(courseRepository).findById("course-123");
        verify(courseRepository).existsByCourseCode("MATH101");
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateCourse_NotFound_ThrowsResourceNotFoundException() {
        when(courseRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courseService.updateCourse("nonexistent", updateRequest));

        verify(courseRepository).findById("nonexistent");
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_Successful() {
        when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));

        courseService.deleteCourse("course-123");

        assertFalse(course.getActive());
        verify(courseRepository).findById("course-123");
        verify(courseRepository).save(course);
    }

    @Test
    void deleteCourse_NotFoundOrInactive_ThrowsResourceNotFoundException() {
        when(courseRepository.findById("nonexistent")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse("nonexistent"));

        course.setActive(false);
        when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));
        assertThrows(ResourceNotFoundException.class, () -> courseService.deleteCourse("course-123"));
    }

    @Test
    void getAllCourses_Successful() {
        when(courseRepository.findByActiveTrueOrderByCourseCodeAsc()).thenReturn(List.of(course));

        List<CourseSummaryResponse> responses = courseService.getAllCourses();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("CS101", responses.get(0).getCourseCode());

        verify(courseRepository).findByActiveTrueOrderByCourseCodeAsc();
    }

    @Test
    void getCourseById_Successful() {
        when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));

        CourseResponse response = courseService.getCourseById("course-123");

        assertNotNull(response);
        assertEquals("CS101", response.getCourseCode());

        verify(courseRepository).findById("course-123");
    }

    @Test
    void getCourseById_Inactive_ThrowsResourceNotFoundException() {
        course.setActive(false);
        when(courseRepository.findById("course-123")).thenReturn(Optional.of(course));

        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById("course-123"));

        verify(courseRepository).findById("course-123");
    }

    @Test
    void getByDepartment_Successful() {
        when(courseRepository.findByDepartmentAndActiveTrueOrderByCourseCodeAsc("CS")).thenReturn(List.of(course));

        List<CourseSummaryResponse> responses = courseService.getByDepartment("CS");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("CS101", responses.get(0).getCourseCode());

        verify(courseRepository).findByDepartmentAndActiveTrueOrderByCourseCodeAsc("CS");
    }

    @Test
    void getBySemester_Successful() {
        when(courseRepository.findBySemesterAndActiveTrueOrderByCourseCodeAsc(1)).thenReturn(List.of(course));

        List<CourseSummaryResponse> responses = courseService.getBySemester(1);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("CS101", responses.get(0).getCourseCode());

        verify(courseRepository).findBySemesterAndActiveTrueOrderByCourseCodeAsc(1);
    }

    @Test
    void getBySemester_InvalidSemester_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> courseService.getBySemester(0));
        assertThrows(BadRequestException.class, () -> courseService.getBySemester(-1));
        assertThrows(BadRequestException.class, () -> courseService.getBySemester(null));
    }

    @Test
    void getElectives_Successful() {
        course.setElective(true);
        when(courseRepository.findByElectiveTrueAndActiveTrueOrderByCourseCodeAsc()).thenReturn(List.of(course));

        List<CourseSummaryResponse> responses = courseService.getElectives();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getElective());

        verify(courseRepository).findByElectiveTrueAndActiveTrueOrderByCourseCodeAsc();
    }

    @Test
    void getMandatoryCourses_Successful() {
        course.setElective(false);
        when(courseRepository.findByElectiveFalseAndActiveTrueOrderByCourseCodeAsc()).thenReturn(List.of(course));

        List<CourseSummaryResponse> responses = courseService.getMandatoryCourses();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertFalse(responses.get(0).getElective());

        verify(courseRepository).findByElectiveFalseAndActiveTrueOrderByCourseCodeAsc();
    }
}
