package com.campusguide.campus.council.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Document(collection = "councils")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Council {

    @Id
    @jakarta.validation.constraints.NotNull(message = "ID must not be null")
    private UUID id;

    @jakarta.validation.constraints.NotBlank(message = "Name must not be blank")
    @Indexed(unique = true)
    private String name;

    @jakarta.validation.constraints.NotBlank(message = "Slug must not be blank")
    @Indexed(unique = true)
    private String slug;

    private String description;

    private String logoUrl;

    @jakarta.validation.constraints.Email(message = "Email must be valid")
    private String email;

    private String contactNumber;

    private String facultyAdvisor;

    private Boolean isActive;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @org.springframework.data.annotation.Version
    private Long version;

    public static class CouncilBuilder {
        public CouncilBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public CouncilBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public CouncilBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public CouncilBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
