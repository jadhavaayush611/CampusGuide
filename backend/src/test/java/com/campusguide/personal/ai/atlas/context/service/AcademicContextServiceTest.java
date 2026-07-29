package com.campusguide.personal.ai.atlas.context.service;

import com.campusguide.campus.academic.progress.entity.StudentProgress;
import com.campusguide.campus.academic.progress.repository.StudentProgressRepository;
import com.campusguide.personal.ai.atlas.context.model.AcademicContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicContextServiceTest {

    @Mock
    private StudentProgressRepository studentProgressRepository;

    private AcademicContextService academicContextService;

    @BeforeEach
    void setUp() {
        academicContextService = new AcademicContextService(studentProgressRepository);
    }

    @Test
    void testGetAcademicContext_WithStudentProgress() {
        String userId = "student-1";
        StudentProgress progress = StudentProgress.builder()
                .studentId(userId)
                .currentGpa(3.9)
                .totalCreditsEarned(75)
                .completedCourseIds(List.of("CS101", "CS201", "CS301"))
                .build();

        when(studentProgressRepository.findByStudentId(userId)).thenReturn(Optional.of(progress));

        AcademicContext context = academicContextService.getAcademicContext(userId, null);

        assertNotNull(context);
        assertEquals(3.9, context.getGpa());
        assertEquals(75, context.getCompletedCredits());
        assertEquals(3, context.getCurrentCourses().size());
        assertEquals("CS101", context.getCurrentCourses().get(0));
    }
}
