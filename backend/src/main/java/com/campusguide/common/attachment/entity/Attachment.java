package com.campusguide.common.attachment.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "attachments")
@CompoundIndexes({
    @CompoundIndex(name = "owner_type_id_idx", def = "{'ownerType': 1, 'ownerId': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @NotNull(message = "ID must not be null")
    private UUID id;

    @NotNull(message = "Owner type must not be null")
    private AttachmentOwnerType ownerType;

    @NotNull(message = "Owner ID must not be null")
    private UUID ownerId;

    @NotBlank(message = "Uploader ID must not be blank")
    @Indexed
    private String uploaderId;

    @NotBlank(message = "Original filename must not be blank")
    private String originalFileName;

    @NotBlank(message = "Stored filename must not be blank")
    private String storedFileName;

    @NotBlank(message = "Content type must not be blank")
    private String contentType;

    @NotNull(message = "File size must not be null")
    private Long fileSize;

    private String storageReference;

    @CreatedDate
    private Instant createdAt;
}
