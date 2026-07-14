package com.campusguide.modules.progress.repository;

import com.campusguide.modules.progress.entity.StudentProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface StudentProgressRepository extends MongoRepository<StudentProgress, String> {

    Optional<StudentProgress> findByStudentId(String studentId);

    List<StudentProgress> findByRoadmapIdOrderByCreatedAtDesc(String roadmapId);

    List<StudentProgress> findAllByOrderByCreatedAtDesc();
}
