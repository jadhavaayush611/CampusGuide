package com.campusguide.personal.planner.repository;

import com.campusguide.personal.planner.entity.StudyGoal;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudyGoalRepository extends MongoRepository<StudyGoal, UUID> {

    List<StudyGoal> findByUserIdOrderByCreatedAtDesc(String userId);

    List<StudyGoal> findByUserId(String userId);

    Optional<StudyGoal> findByIdAndUserId(UUID id, String userId);

    boolean existsByIdAndUserId(UUID id, String userId);
}
