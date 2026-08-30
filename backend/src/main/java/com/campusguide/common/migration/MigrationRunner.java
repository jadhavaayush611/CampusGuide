package com.campusguide.common.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class MigrationRunner implements SmartInitializingSingleton {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MigrationRepository migrationRepository;

    @Autowired(required = false)
    private List<Migration> migrations = new ArrayList<>();

    @Override
    public void afterSingletonsInstantiated() {
        runMigrations();
        validateAndCreateIndexes();
    }

    private void runMigrations() {
        log.info("Starting database migrations execution...");
        if (migrations == null || migrations.isEmpty()) {
            log.info("No migrations registered.");
            return;
        }

        List<Migration> sortedMigrations = new ArrayList<>(migrations);
        sortedMigrations.sort(Comparator.comparing(Migration::getVersion));

        for (Migration migration : sortedMigrations) {
            String version = migration.getVersion();
            if (migrationRepository.existsById(version)) {
                log.debug("Migration {} already executed successfully. Skipping.", version);
                continue;
            }

            log.info("Executing database migration {}: {}", version, migration.getDescription());
            long start = System.currentTimeMillis();
            try {
                migration.execute(mongoTemplate);
                long elapsed = System.currentTimeMillis() - start;
                DatabaseMigration logEntry = new DatabaseMigration(
                        version,
                        migration.getDescription(),
                        Instant.now(),
                        elapsed,
                        true
                );
                migrationRepository.save(logEntry);
                log.info("Migration {} executed successfully in {} ms", version, elapsed);
            } catch (Exception e) {
                long elapsed = System.currentTimeMillis() - start;
                log.error("Migration {} failed after {} ms: {}", version, elapsed, e.getMessage(), e);
                try {
                    DatabaseMigration logEntry = new DatabaseMigration(
                            version,
                            migration.getDescription(),
                            Instant.now(),
                            elapsed,
                            false
                    );
                    migrationRepository.save(logEntry);
                } catch (Exception ex) {
                    log.error("Failed to persist migration failure log for version {}", version, ex);
                }
                throw new RuntimeException("Database migration failed: " + version, e);
            }
        }
        log.info("All database migrations completed successfully.");
    }

    private void validateAndCreateIndexes() {
        log.info("Starting startup index validation and creation...");
        try {
            MongoMappingContext mappingContext = (MongoMappingContext) mongoTemplate.getConverter().getMappingContext();
            for (MongoPersistentEntity<?> entity : mappingContext.getPersistentEntities()) {
                if (entity.isAnnotationPresent(Document.class)) {
                    Class<?> clazz = entity.getType();
                    Document docAnno = entity.findAnnotation(Document.class);
                    String collection = docAnno != null ? docAnno.collection() : clazz.getSimpleName().toLowerCase();
                    log.info("Resolving and validating indexes for collection: {}", collection);

                    IndexOperations indexOps = mongoTemplate.indexOps(clazz);
                    IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
                    resolver.resolveIndexFor(clazz).forEach(index -> {
                        try {
                            indexOps.ensureIndex(index);
                            log.debug("Validated/Created index: {} on collection {}", index, collection);
                        } catch (Exception e) {
                            if (e.getMessage() != null && e.getMessage().contains("IndexOptionsConflict")) {
                                log.warn("IndexOptionsConflict on collection {}, recreating index: {}", collection, index);
                                try {
                                    indexOps.dropAllIndexes();
                                    indexOps.ensureIndex(index);
                                } catch (Exception inner) {
                                    log.error("Failed to re-create index {} after conflict: {}", index, inner.getMessage());
                                }
                            } else {
                                log.error("Failed to create index {} on collection {}: {}", index, collection, e.getMessage());
                                throw new RuntimeException("Startup index creation failed for collection: " + collection, e);
                            }
                        }
                    });
                }
            }
            log.info("Startup index validation completed successfully.");
        } catch (Exception e) {
            log.error("Startup validation of MongoDB indexes failed: {}", e.getMessage(), e);
            throw new RuntimeException("Startup database validation failed", e);
        }
    }
}
