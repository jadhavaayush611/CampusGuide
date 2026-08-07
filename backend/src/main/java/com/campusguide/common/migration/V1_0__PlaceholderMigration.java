package com.campusguide.common.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class V1_0__PlaceholderMigration implements Migration {

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getDescription() {
        return "Placeholder migration to initialize the database migration system.";
    }

    @Override
    public void execute(MongoTemplate mongoTemplate) throws Exception {
        log.info("Running initial database migration V1.0...");
    }
}
