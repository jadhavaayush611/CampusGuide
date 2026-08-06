package com.campusguide.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration preparing Swagger documentation for Bearer JWT authentication.
 * Includes complete contact, license, tag, and production/staging server configurations.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact()
                .name("CampusGuide Core Team")
                .email("core-devs@campusguide.example.com")
                .url("https://campusguide.example.com");

        License license = new License()
                .name("Apache 2.0 License")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");

        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");

        Server stagingServer = new Server()
                .url("https://staging-api.campusguide.example.com")
                .description("Staging Server");

        Server prodServer = new Server()
                .url("https://api.campusguide.example.com")
                .description("Production Gateway Server");

        List<Tag> tags = List.of(
                new Tag().name("Platform").description("Authentication, User Settings, and Global Search Services"),
                new Tag().name("Academic").description("Courses, Semesters, Degree Roadmaps, and Progress Tracking"),
                new Tag().name("Campus").description("Student Councils, Discussion Forums, Events, and Resource Sharing"),
                new Tag().name("Personal").description("FCM In-App Notifications, Resume Builder, and AI Advisor (Atlas)")
        );

        return new OpenAPI()
                .info(new Info()
                        .title("CampusGuide REST API")
                        .version("1.0.0")
                        .description("Centralized backend services powering the CampusGuide digital community portal.")
                        .contact(contact)
                        .license(license))
                .servers(List.of(devServer, stagingServer, prodServer))
                .tags(tags)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
