package com.campusguide.personal.ai.atlas.knowledge.ingestion;

import com.campusguide.campus.resource.entity.Resource;
import com.campusguide.campus.resource.repository.ResourceRepository;
import com.campusguide.campus.notice.entity.Notice;
import com.campusguide.campus.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AtlasKnowledgeInitializer implements CommandLineRunner {

    private final ResourceRepository resourceRepository;
    private final NoticeRepository noticeRepository;
    private final KnowledgeIngestionService ingestionService;

    @Override
    public void run(String... args) {
        log.info("Starting Atlas Knowledge Base RAG seeding...");
        
        try {
            // Seed Resources
            List<Resource> resources = resourceRepository.findByIsDeletedFalseOrderByCreatedAtDesc();
            int resCount = 0;
            for (Resource res : resources) {
                String text = String.format("Resource Title: %s\nDescription: %s\nURL/Link: %s\nUploaded By: %s\nTags: %s\nCouncil ID: %s\nCommunity ID: %s",
                        res.getTitle(), res.getDescription(), res.getDownloadUrl(), res.getUploaderId(), 
                        res.getTags() != null ? String.join(", ", res.getTags()) : "",
                        res.getCouncilId(), res.getCommunityId());
                ingestionService.ingestText(text, res.getTitle(), "db://resources/" + res.getId(), "resources");
                resCount++;
            }
            log.info("Successfully indexed {} resources in Atlas Vector Store.", resCount);

            // Seed Notices
            List<Notice> notices = noticeRepository.findByIsPublishedTrue();
            int noticeCount = 0;
            for (Notice notice : notices) {
                String text = String.format("Notice Title: %s\nContent: %s\nPriority: %s\nCategory: %s\nCouncil ID: %s",
                        notice.getTitle(), notice.getContent(), notice.getPriority(), notice.getCategory(), notice.getCouncilId());
                ingestionService.ingestText(text, notice.getTitle(), "db://notices/" + notice.getId(), "notices");
                noticeCount++;
            }
            log.info("Successfully indexed {} notices in Atlas Vector Store.", noticeCount);
            
        } catch (Exception e) {
            log.error("Failed to seed Atlas Knowledge Base RAG", e);
        }
    }
}
