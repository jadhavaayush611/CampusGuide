package com.campusguide.common.attachment.dto;

import com.campusguide.common.attachment.entity.AttachmentOwnerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {

    private UUID id;
    private AttachmentOwnerType ownerType;
    private UUID ownerId;
    private String uploaderId;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
    private LocalDateTime createdAt;
}
