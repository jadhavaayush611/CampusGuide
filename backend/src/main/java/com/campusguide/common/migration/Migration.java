package com.campusguide.common.migration;

import org.springframework.data.mongodb.core.MongoTemplate;

public interface Migration {
    String getVersion();
    String getDescription();
    void execute(MongoTemplate mongoTemplate) throws Exception;
}
