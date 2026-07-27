package com.campusguide.personal.achievement.repository;

import com.campusguide.personal.achievement.entity.AchievementCategory;
import com.campusguide.personal.achievement.entity.AchievementProgress;
import com.campusguide.personal.achievement.entity.AchievementStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AchievementProgressRepository extends MongoRepository<AchievementProgress, UUID> {

    List<AchievementProgress> findByUserId(UUID userId);

    List<AchievementProgress> findByUserIdAndCategory(UUID userId, AchievementCategory category);

    List<AchievementProgress> findByUserIdAndStatus(UUID userId, AchievementStatus status);

    List<AchievementProgress> findByUserIdAndCategoryAndStatus(UUID userId, AchievementCategory category, AchievementStatus status);

    Optional<AchievementProgress> findByIdAndUserId(UUID id, UUID userId);

    Optional<AchievementProgress> findByUserIdAndAchievementCode(UUID userId, String achievementCode);

    boolean existsByUserIdAndAchievementCode(UUID userId, String achievementCode);

    boolean existsByUserIdAndAchievementCodeAndIdNot(UUID userId, String achievementCode, UUID id);
}
