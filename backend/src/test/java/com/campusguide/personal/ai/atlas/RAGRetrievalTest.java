package com.campusguide.personal.ai.atlas;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.ContextEngine;
import com.campusguide.personal.ai.atlas.context.evidence.EvidenceBundle;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RAGRetrievalTest {

    @Autowired
    private ContextEngine contextEngine;

    private boolean containsSnippet(AtlasContext context, String keyword) {
        if (context == null || context.getEvidenceBundles() == null) return false;
        String keywordLower = keyword.toLowerCase();
        for (EvidenceBundle bundle : context.getEvidenceBundles().values()) {
            if (bundle.getEvidences() == null) continue;
            for (RetrievalEvidence ev : bundle.getEvidences()) {
                if (ev.getContentSnippet() != null && ev.getContentSnippet().toLowerCase().contains(keywordLower)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    @DisplayName("RAG Campus layout queries should return correct locations")
    void testCampusLayoutRetrieval() {
        // Library
        AtlasContext context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("Where is the library?").build());
        assertTrue(containsSnippet(context, "Library"), "Should locate Library");
        assertTrue(containsSnippet(context, "1st Floor"), "Library should be on 1st floor");

        // AIDS floor
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("Which floor is AIDS on?").build());
        assertTrue(containsSnippet(context, "AIDS"), "Should locate AIDS department");
        assertTrue(containsSnippet(context, "2nd Floor"), "AIDS department should be on 2nd floor");

        // CMPN floor
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("Where is the CMPN department?").build());
        assertTrue(containsSnippet(context, "Computer Engineering"), "Should locate CMPN department");
        assertTrue(containsSnippet(context, "3rd Floor"), "CMPN department should be on 3rd floor");

        // Principal's office
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("Where is the principal's office?").build());
        assertTrue(containsSnippet(context, "Principal's Office"), "Should locate Principal's Office");
        assertTrue(containsSnippet(context, "Ground Floor"), "Principal's office should be on Ground Floor");

        // Lifts
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("How many lifts are there?").build());
        assertTrue(containsSnippet(context, "four lifts"), "Should contain four lifts layout information");

        // Washrooms
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("Where are the washrooms?").build());
        assertTrue(containsSnippet(context, "four washrooms per floor"), "Should contain washrooms layout information");

        // Amphitheatre
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("Where is the amphitheatre?").build());
        assertTrue(containsSnippet(context, "Amphitheatre"), "Should locate Amphitheatre");
        assertTrue(containsSnippet(context, "2nd Floor"), "Amphitheatre should be on 2nd floor");
    }

    @Test
    @DisplayName("RAG Academic batch queries should return correct batch allocations")
    void testAcademicBatchRetrieval() {
        // CMPN batches
        AtlasContext context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("What batches does CMPN have?").build());
        assertTrue(containsSnippet(context, "CMPN: D2A, D2B, D2C (FE)"), "Should retrieve CMPN batches");

        // AIDS batches
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("What batches does AIDS have?").build());
        assertTrue(containsSnippet(context, "AIDS: D1ADA, D1ADB (FE)"), "Should retrieve AIDS batches");

        // D12A batch
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("What batch is D12A?").build());
        assertTrue(containsSnippet(context, "D12A"), "Should retrieve info for D12A");

        // TE batches
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("What batches exist in TE?").build());
        assertTrue(containsSnippet(context, "TE"), "Should retrieve TE batches");
    }

    @Test
    @DisplayName("RAG Organizations queries should return correct councils and communities")
    void testOrganizationsRetrieval() {
        // ISTE
        AtlasContext context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("Tell me about ISTE").build());
        assertTrue(containsSnippet(context, "ISTE"), "Should retrieve ISTE information");

        // CC
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("Which council handles cultural activities?").build());
        assertTrue(containsSnippet(context, "CC: Cultural Council"), "Should retrieve Cultural Council");

        // SORT
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("Which council handles social work?").build());
        assertTrue(containsSnippet(context, "SORT: Social Outreach"), "Should retrieve SORT information");

        // GDG
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("What AI communities exist?").build());
        assertTrue(containsSnippet(context, "Google Developer Groups"), "Should retrieve GDG community");

        // AI & ML Club
        assertTrue(containsSnippet(context, "AI & ML Club"), "Should retrieve AI & ML Club");
    }

    @Test
    @DisplayName("Negative queries should not match layout, batches, or councils")
    void testNegativeRAGRetrieval() {
        // nonexistent campus facility
        AtlasContext context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("Where is the space elevator room?").build());
        assertFalse(containsSnippet(context, "space elevator"), "Should not retrieve space elevator");

        // nonexistent council
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("Tell me about the wizarding council").build());
        assertFalse(containsSnippet(context, "wizarding council"), "Should not retrieve wizarding council");

        // nonexistent batch
        context = contextEngine.buildContext(AtlasChatRequest.builder().prompt("What department owns batch D99X?").build());
        assertFalse(containsSnippet(context, "D99X"), "Should not match nonexistent batch");
    }
}
