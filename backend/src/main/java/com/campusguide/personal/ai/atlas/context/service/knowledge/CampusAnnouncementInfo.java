package com.campusguide.personal.ai.atlas.context.service.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampusAnnouncementInfo {
    private String announcementId;
    private String title;
    private String content;
    private String category;
    private String priority;
    private long publishedAt;
    private long expiresAt;
}
