package com.campusguide.common.migration;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MigrationRepository extends MongoRepository<DatabaseMigration, String> {
}
