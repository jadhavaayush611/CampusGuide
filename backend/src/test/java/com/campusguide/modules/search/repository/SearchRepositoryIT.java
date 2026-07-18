package com.campusguide.modules.search.repository;

import com.campusguide.modules.course.entity.Course;
import com.campusguide.modules.course.repository.CourseRepository;
import com.campusguide.modules.roadmap.entity.Roadmap;
import com.campusguide.modules.roadmap.repository.RoadmapRepository;
import com.campusguide.modules.community.entity.Community;
import com.campusguide.modules.community.repository.CommunityRepository;
import com.campusguide.modules.resource.entity.Resource;
import com.campusguide.modules.resource.repository.ResourceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SearchRepositoryIT {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @BeforeEach
    void setUp() {
        cleanup();

        // Save some courses
        Course c1 = Course.builder().courseCode("CS201").courseName("Advanced Java Programming").description("Learn Java patterns").active(true).build();
        Course c2 = Course.builder().courseCode("CS202").courseName("Python Basics").description("Intro to Python").active(false).build(); // Inactive
        Course c3 = Course.builder().courseCode("CS203").courseName("Data Structures").description("Java Collections Framework").active(true).build();
        courseRepository.saveAll(List.of(c1, c2, c3));

        // Save roadmaps
        Roadmap r1 = Roadmap.builder().title("Web Development Guide").description("Learn HTML, CSS, JS").isDeleted(false).build();
        Roadmap r2 = Roadmap.builder().title("Mobile Dev Guide").description("Learn Android and iOS").isDeleted(true).build(); // Deleted
        roadmapRepository.saveAll(List.of(r1, r2));

        // Save communities
        Community com1 = Community.builder().name("Java Enthusiasts").description("Java discussions").isActive(true).build();
        Community com2 = Community.builder().name("Python Lovers").description("Python discussions").isActive(false).build(); // Inactive
        communityRepository.saveAll(List.of(com1, com2));

        // Save resources
        Resource res1 = Resource.builder().title("Java Cheatsheet").description("Quick reference guide").tags(List.of("java", "reference")).isDeleted(false).build();
        Resource res2 = Resource.builder().title("Docker Guide").description("Docker commands").tags(List.of("docker")).isDeleted(true).build(); // Deleted
        resourceRepository.saveAll(List.of(res1, res2));
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        courseRepository.deleteAll();
        roadmapRepository.deleteAll();
        communityRepository.deleteAll();
        resourceRepository.deleteAll();
    }

    @Test
    void testCourseSearch_CaseInsensitiveAndPartialAndActiveOnly() {
        List<Course> courses = courseRepository.findByActiveTrueAndCourseNameContainingIgnoreCaseOrActiveTrueAndCourseCodeContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCase(
                "java", "java", "java"
        );
        assertEquals(2, courses.size());
        assertTrue(courses.stream().anyMatch(c -> c.getCourseCode().equals("CS201")));
        assertTrue(courses.stream().anyMatch(c -> c.getCourseCode().equals("CS203")));

        List<Course> coursesCode = courseRepository.findByActiveTrueAndCourseNameContainingIgnoreCaseOrActiveTrueAndCourseCodeContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCase(
                "CS201", "CS201", "CS201"
        );
        assertEquals(1, coursesCode.size());
        assertEquals("CS201", coursesCode.get(0).getCourseCode());
    }

    @Test
    void testRoadmapSearch_CaseInsensitiveAndPartialAndNonDeleted() {
        List<Roadmap> roadmaps = roadmapRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCase(
                "web", "web"
        );
        assertEquals(1, roadmaps.size());
        assertEquals("Web Development Guide", roadmaps.get(0).getTitle());

        List<Roadmap> devRoadmaps = roadmapRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCase(
                "dev", "dev"
        );
        assertEquals(1, devRoadmaps.size());
    }

    @Test
    void testCommunitySearch_CaseInsensitiveAndPartialAndActiveOnly() {
        List<Community> communities = communityRepository.findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndDescriptionContainingIgnoreCase(
                "java", "java"
        );
        assertEquals(1, communities.size());
        assertEquals("Java Enthusiasts", communities.get(0).getName());
    }

    @Test
    void testResourceSearch_CaseInsensitiveAndTagsAndNonDeleted() {
        List<Resource> resources = resourceRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndTagsContainingIgnoreCase(
                "reference", "reference", "reference"
        );
        assertEquals(1, resources.size());
        assertEquals("Java Cheatsheet", resources.get(0).getTitle());
    }
}
