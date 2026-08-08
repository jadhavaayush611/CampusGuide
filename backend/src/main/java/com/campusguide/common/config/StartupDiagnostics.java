package com.campusguide.common.config;

import com.campusguide.CampusguideApplication;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * Startup Diagnostics Runner that logs essential system parameters
 * (active profile, Java version, Spring Boot version, MongoDB connection status,
 * and application version) once the context is initialized.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartupDiagnostics implements ApplicationRunner {

    private final Environment environment;
    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        String activeProfiles = Arrays.toString(environment.getActiveProfiles());
        String javaVersion = System.getProperty("java.version");
        String springBootVersion = SpringBootVersion.getVersion();
        
        String appVersion = CampusguideApplication.class.getPackage().getImplementationVersion();
        if (appVersion == null) {
            appVersion = "1.0.0-MVP"; // Fallback to MVP version in pom.xml
        }

        String mongoStatus;
        try {
            Document pingResult = mongoTemplate.executeCommand("{ping: 1}");
            mongoStatus = "CONNECTED (ping response: " + pingResult.toJson() + ")";
        } catch (Exception e) {
            mongoStatus = "DISCONNECTED (" + e.getMessage() + ")";
            log.error("MongoDB connectivity check failed on startup", e);
        }

        log.info("========================================================================");
        log.info("CampusGuide Application Startup Diagnostics:");
        log.info("  Active Profile(s): {}", activeProfiles);
        log.info("  Java Version:      {}", javaVersion);
        log.info("  Spring Boot Ver:   {}", springBootVersion);
        log.info("  Application Ver:   {}", appVersion);
        log.info("  MongoDB Status:    {}", mongoStatus);
        log.info("========================================================================");
    }
}
