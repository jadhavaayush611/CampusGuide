package com.campusguide.campus.resource.repository;

import com.campusguide.campus.resource.entity.Resource;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ResourceRepository extends MongoRepository<Resource, String> {

    List<Resource> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Resource> findByTitleContainingIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(String title, String description);

    List<Resource> findByTagsIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(String tag);

    List<Resource> findByUploaderIdAndIsDeletedFalseOrderByCreatedAtDesc(String uploaderId);

    List<Resource> findByCouncilIdAndIsDeletedFalseOrderByCreatedAtDesc(String councilId);

    List<Resource> findByCommunityIdAndIsDeletedFalseOrderByCreatedAtDesc(String communityId);

    List<Resource> findByTagsContainingAndIsDeletedFalseOrderByCreatedAtDesc(String tag);

    List<Resource> findByTitleContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(String title);

    long countByIsDeletedFalse();

    List<Resource> findByIsDeletedFalseAndTitleContainingIgnoreCaseOrIsDeletedFalseAndDescriptionContainingIgnoreCaseOrIsDeletedFalseAndTagsContainingIgnoreCase(
            String title, String description, String tag
    );
}
