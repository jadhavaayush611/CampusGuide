package com.campusguide.common.attachment.service;

import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.repository.NoticeRepository;
import com.campusguide.common.attachment.entity.AttachmentOwnerType;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachmentAuthorizer {

    private final PlannerTaskRepository plannerTaskRepository;
    private final NoticeRepository noticeRepository;
    private final CurrentUserService currentUserService;

    public void authorizeUpload(UserDetails userDetails, AttachmentOwnerType ownerType, UUID ownerId) {
        String currentUserId = currentUserService.getCurrentUserId(userDetails);

        if (ownerType == AttachmentOwnerType.PLANNER_TASK) {
            PlannerTask task = plannerTaskRepository.findById(ownerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Planner task not found with id: " + ownerId));

            if (!task.getUserId().equals(currentUserId)) {
                throw new ResourceNotFoundException("Planner task not found with id: " + ownerId);
            }
        } else if (ownerType == AttachmentOwnerType.NOTICE) {
            Notice notice = noticeRepository.findById(ownerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + ownerId));

            if (!isAdmin(userDetails)) {
                throw new AccessDeniedException("Only administrators can attach files to notices");
            }
        } else {
            throw new IllegalArgumentException("Unsupported owner type: " + ownerType);
        }
    }

    public void authorizeRead(UserDetails userDetails, AttachmentOwnerType ownerType, UUID ownerId) {
        if (ownerType == AttachmentOwnerType.PLANNER_TASK) {
            String currentUserId = currentUserService.getCurrentUserId(userDetails);
            PlannerTask task = plannerTaskRepository.findById(ownerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Planner task not found with id: " + ownerId));

            if (!task.getUserId().equals(currentUserId)) {
                throw new ResourceNotFoundException("Planner task not found with id: " + ownerId);
            }
        } else if (ownerType == AttachmentOwnerType.NOTICE) {
            Notice notice = noticeRepository.findById(ownerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + ownerId));

            // If notice is not published, only admins can view its attachments
            if (!Boolean.TRUE.equals(notice.getIsPublished()) && !isAdmin(userDetails)) {
                throw new AccessDeniedException("Access denied to attachments of unpublished notice");
            }
        } else {
            throw new IllegalArgumentException("Unsupported owner type: " + ownerType);
        }
    }

    public void authorizeDelete(UserDetails userDetails, AttachmentOwnerType ownerType, UUID ownerId) {
        String currentUserId = currentUserService.getCurrentUserId(userDetails);

        if (ownerType == AttachmentOwnerType.PLANNER_TASK) {
            PlannerTask task = plannerTaskRepository.findById(ownerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Planner task not found with id: " + ownerId));

            if (!task.getUserId().equals(currentUserId)) {
                throw new ResourceNotFoundException("Planner task not found with id: " + ownerId);
            }
        } else if (ownerType == AttachmentOwnerType.NOTICE) {
            Notice notice = noticeRepository.findById(ownerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + ownerId));

            if (!isAdmin(userDetails)) {
                throw new AccessDeniedException("Only administrators can delete notice attachments");
            }
        } else {
            throw new IllegalArgumentException("Unsupported owner type: " + ownerType);
        }
    }

    private boolean isAdmin(UserDetails userDetails) {
        if (userDetails == null || userDetails.getAuthorities() == null) {
            return false;
        }
        return userDetails.getAuthorities().stream().anyMatch(a ->
                "ROLE_SUPER_ADMIN".equals(a.getAuthority()) || "ROLE_COUNCIL_ADMIN".equals(a.getAuthority()));
    }
}
