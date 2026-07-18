package com.campusguide.modules.community.repository;

import com.campusguide.modules.community.entity.Community;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CommunityRepository extends MongoRepository<Community, String> {

    Optional<Community> findByName(String name);

    boolean existsByName(String name);

    List<Community> findByCouncilId(String councilId);

    List<Community> findByIsActiveTrue();

    long countByIsActiveTrue();

    List<Community> findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndDescriptionContainingIgnoreCase(
            String name, String description
    );
}
