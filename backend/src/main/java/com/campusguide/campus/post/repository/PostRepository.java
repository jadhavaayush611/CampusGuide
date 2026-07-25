package com.campusguide.campus.post.repository;

import com.campusguide.campus.post.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {

    List<Post> findByCommunityId(String communityId);

    List<Post> findByAuthorId(String authorId);

    List<Post> findByIsDeletedFalse();

    List<Post> findByCommunityIdAndIsDeletedFalse(String communityId);

    List<Post> findByAuthorIdAndIsDeletedFalse(String authorId);

    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc();
}
