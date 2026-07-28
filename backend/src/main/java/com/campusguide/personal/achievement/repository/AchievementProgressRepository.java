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

    List<AchievementProgress> findByUserId(String userId);

    List<AchievementProgress> findByUserIdAndCategory(String userId, AchievementCategory category);

    List<AchievementProgress> findByUserIdAndStatus(String userId, AchievementStatus status);

    List<AchievementProgress> findByUserIdAndCategoryAndStatus(String userId, AchievementCategory category, AchievementStatus status);

    Optional<AchievementProgress> findByIdAndUserId(UUID id, String userId);

    Optional<AchievementProgress> findByUserIdAndAchievementCode(String userId, String achievementCode);

    boolean existsByUserIdAndAchievementCode(String userId, String achievementCode);

    boolean existsByUserIdAndAchievementCodeAndIdNot(String userId, String achievementCode, UUID id);
}
