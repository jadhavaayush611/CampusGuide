package com.campusguide.common.attachment.repository;

import com.campusguide.common.attachment.entity.Attachment;
import com.campusguide.common.attachment.entity.AttachmentOwnerType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends MongoRepository<Attachment, UUID> {

    List<Attachment> findByOwnerTypeAndOwnerId(AttachmentOwnerType ownerType, UUID ownerId);

    List<Attachment> findByOwnerTypeAndOwnerIdIn(AttachmentOwnerType ownerType, List<UUID> ownerIds);

    List<Attachment> findByUploaderId(String uploaderId);

    void deleteByOwnerTypeAndOwnerId(AttachmentOwnerType ownerType, UUID ownerId);

    long countByOwnerTypeAndOwnerId(AttachmentOwnerType ownerType, UUID ownerId);
}
