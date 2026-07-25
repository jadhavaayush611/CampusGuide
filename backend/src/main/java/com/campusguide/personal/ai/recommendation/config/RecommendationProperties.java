package com.campusguide.personal.ai.recommendation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "recommendation")
@Data
public class RecommendationProperties {

    private final Academic academic = new Academic();
    private final Event event = new Event();
    private final Community community = new Community();
    private final Resource resource = new Resource();

    @Data
    public static class Academic {
        private double currentSemesterWeight = 0.85;
        private double prerequisiteWeight = 0.90;
        private double departmentWeight = 0.70;
        private double missingPrereqBoost = 0.80;
    }

    @Data
    public static class Event {
        private double baseWeight = 0.40;
        private double departmentWeight = 0.40;
        private double communityWeight = 0.15;
        private double deadlineWeight = 0.80;
    }

    @Data
    public static class Community {
        private double baseWeight = 0.40;
        private double departmentWeight = 0.45;
        private double interestWeight = 0.15;
    }

    @Data
    public static class Resource {
        private double baseWeight = 0.30;
        private double enrolledWeight = 0.90;
        private double roadmapWeight = 0.70;
        private double departmentWeight = 0.55;
    }
}
