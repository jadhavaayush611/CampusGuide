package com.campusguide.modules.resource.repository;

import com.campusguide.modules.resource.entity.Resource;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ResourceRepository extends MongoRepository<Resource, String> {

    List<Resource> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Resource> findByTitleContainingIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(String title, String description);

    List<Resource> findByTagsIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(String tag);

    List<Resource> findByUploaderIdAndIsDeletedFalseOrderByCreatedAtDesc(String uploaderId);

    List<Resource> findByCouncilIdAndIsDeletedFalseOrderByCreatedAtDesc(String councilId);

    List<Resource> findByCommunityIdAndIsDeletedFalseOrderByCreatedAtDesc(String communityId);

    List<Resource> findByTagsContainingAndIsDeletedFalse(String tag);

    List<Resource> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title);
}
