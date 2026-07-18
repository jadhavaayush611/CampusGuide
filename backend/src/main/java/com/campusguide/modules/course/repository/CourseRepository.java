package com.campusguide.modules.course.repository;

import com.campusguide.modules.course.entity.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    List<Course> findByActiveTrueOrderByCourseCodeAsc();

    Optional<Course> findByIdAndActiveTrue(String id);

    List<Course> findByDepartmentAndActiveTrueOrderByCourseCodeAsc(String department);

    List<Course> findBySemesterAndActiveTrueOrderByCourseCodeAsc(Integer semester);

    Optional<Course> findByCourseCodeAndActiveTrue(String courseCode);

    Optional<Course> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    List<Course> findByElectiveTrueAndActiveTrueOrderByCourseCodeAsc();

    List<Course> findByElectiveFalseAndActiveTrueOrderByCourseCodeAsc();

    List<Course> findByActiveTrueAndCourseNameContainingIgnoreCaseOrActiveTrueAndCourseCodeContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCase(
            String courseName, String courseCode, String description
    );
}

