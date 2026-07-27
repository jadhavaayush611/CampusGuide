package com.campusguide.personal.planner.repository;

import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlannerTaskRepository extends MongoRepository<PlannerTask, UUID> {

    List<PlannerTask> findByUserIdOrderByDueAtAsc(UUID userId);

    List<PlannerTask> findByUserId(UUID userId);

    Optional<PlannerTask> findByIdAndUserId(UUID id, UUID userId);

    List<PlannerTask> findByUserIdAndStatus(UUID userId, TaskStatus status);

    boolean existsByIdAndUserId(UUID id, UUID userId);
}
