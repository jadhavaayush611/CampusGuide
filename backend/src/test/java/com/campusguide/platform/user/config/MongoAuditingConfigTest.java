package com.campusguide.platform.user.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import static org.junit.jupiter.api.Assertions.*;

class MongoAuditingConfigTest {

    @Test
    @DisplayName("MongoAuditingConfig should be annotated with @Configuration and @EnableMongoAuditing")
    void testMongoAuditingConfigAnnotations() {
        Class<MongoAuditingConfig> clazz = MongoAuditingConfig.class;
        assertTrue(clazz.isAnnotationPresent(Configuration.class), "Should be annotated with @Configuration");
        assertTrue(clazz.isAnnotationPresent(EnableMongoAuditing.class), "Should be annotated with @EnableMongoAuditing");
    }
}
