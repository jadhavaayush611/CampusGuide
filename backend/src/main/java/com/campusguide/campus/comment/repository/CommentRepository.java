package com.campusguide.campus.comment.repository;

import com.campusguide.campus.comment.entity.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {

    List<Comment> findByPostId(String postId);

    List<Comment> findByAuthorId(String authorId);

    List<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(String postId);

    List<Comment> findByAuthorIdAndIsDeletedFalse(String authorId);
}
