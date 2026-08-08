package com.campusguide.campus.notice.repository;

import com.campusguide.campus.notice.entity.Notice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoticeRepository extends MongoRepository<Notice, UUID> {

    Optional<Notice> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Notice> findByIsPublishedTrue();

    @org.springframework.data.mongodb.repository.Query("{ 'isPublished': true, '$and': [ " +
           "  { '$or': [ { 'publishedAt': null }, { 'publishedAt': { '$lte': ?0 } } ] }, " +
           "  { '$or': [ { 'expiresAt': null }, { 'expiresAt': { '$gt': ?0 } } ] } " +
           "] }")
    List<Notice> findActiveNotices(java.time.LocalDateTime now);

    List<Notice> findByCouncilId(UUID councilId);
}
