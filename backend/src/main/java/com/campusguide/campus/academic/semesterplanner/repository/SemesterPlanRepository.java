package com.campusguide.campus.academic.semesterplanner.repository;

import com.campusguide.campus.academic.semesterplanner.entity.SemesterPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface SemesterPlanRepository extends MongoRepository<SemesterPlan, String> {

    List<SemesterPlan> findByStudentIdOrderBySemesterNumberAsc(String studentId);

    List<SemesterPlan> findByRoadmapIdOrderBySemesterNumberAsc(String roadmapId);

    Optional<SemesterPlan> findByStudentIdAndSemesterNumber(String studentId, Integer semesterNumber);

    List<SemesterPlan> findByFinalizedFalseOrderBySemesterNumberAsc();
}
