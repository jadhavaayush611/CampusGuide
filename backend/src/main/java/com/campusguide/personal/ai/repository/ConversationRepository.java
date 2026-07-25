package com.campusguide.personal.ai.repository;

import com.campusguide.personal.ai.entity.Conversation;
import com.campusguide.personal.ai.enums.ConversationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    List<Conversation> findByUserId(String userId);
    List<Conversation> findByUserIdAndStatus(String userId, ConversationStatus status);
    Optional<Conversation> findByIdAndUserId(String id, String userId);
    boolean existsByIdAndUserId(String id, String userId);
}
