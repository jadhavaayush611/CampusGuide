package com.campusguide.platform.user.repository;

import com.campusguide.platform.user.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByIsVerifiedTrue();
}
