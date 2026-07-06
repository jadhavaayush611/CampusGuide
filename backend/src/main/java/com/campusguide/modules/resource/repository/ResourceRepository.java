package com.campusguide.modules.resource.repository;

import com.campusguide.modules.resource.entity.Resource;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ResourceRepository extends MongoRepository<Resource, String> {

    List<Resource> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Resource> findByUploaderIdAndIsDeletedFalse(String uploaderId);

    List<Resource> findByCouncilIdAndIsDeletedFalse(String councilId);

    List<Resource> findByCommunityIdAndIsDeletedFalse(String communityId);

    List<Resource> findByTagsContainingAndIsDeletedFalse(String tag);

    List<Resource> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title);
}
