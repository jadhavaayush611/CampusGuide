package com.campusguide.academic.roadmap.repository;

import com.campusguide.academic.roadmap.entity.Roadmap;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface RoadmapRepository extends MongoRepository<Roadmap, String> {

    List<Roadmap> findByIsDeletedFalseOrderByCreatedAtDesc();

    Optional<Roadmap> findByIdAndIsDeletedFalse(String id);

    List<Roadmap> findByCreatedByAndIsDeletedFalseOrderByCreatedAtDesc(String createdBy);

    List<Roadmap> findByDegreeProgramIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(String degreeProgram);

    List<Roadmap> findByDepartmentIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(String department);

    long countByIsDeletedFalse();

    List<Roadmap> findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCase(
            String title, String description
    );
}
