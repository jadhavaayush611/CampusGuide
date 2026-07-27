package com.campusguide.campus.council.repository;

import com.campusguide.campus.council.entity.Council;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouncilRepository extends MongoRepository<Council, UUID> {

    Optional<Council> findBySlug(String slug);

    Optional<Council> findByName(String name);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
