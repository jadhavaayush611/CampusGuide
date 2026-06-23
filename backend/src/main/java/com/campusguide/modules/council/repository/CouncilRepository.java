package com.campusguide.modules.council.repository;

import com.campusguide.modules.council.entity.Council;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CouncilRepository extends MongoRepository<Council, String> {

    Optional<Council> findByName(String name);

    boolean existsByName(String name);

    List<Council> findByIsActiveTrue();

    List<Council> findByCategory(String category);
}
