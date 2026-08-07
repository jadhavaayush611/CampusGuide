package com.campusguide.campus.community.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Document(collection = "communities")
@CompoundIndexes({
    @CompoundIndex(name = "council_active_idx", def = "{'councilId': 1, 'isActive': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Community {

    @Id
    private String id;

    @jakarta.validation.constraints.NotBlank(message = "Name must not be blank")
    @Indexed(unique = true)
    private String name;

    private String description;

    private String bannerUrl;

    @jakarta.validation.constraints.NotBlank(message = "Council ID must not be blank")
    @Indexed
    private String councilId;

    private Integer memberCount;

    private Boolean isActive;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @org.springframework.data.annotation.Version
    private Long version;

    public static class CommunityBuilder {
        public CommunityBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public CommunityBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public CommunityBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public CommunityBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
