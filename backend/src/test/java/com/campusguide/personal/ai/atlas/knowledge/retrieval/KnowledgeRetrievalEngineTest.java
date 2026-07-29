package com.campusguide.personal.ai.atlas.knowledge.retrieval;

import com.campusguide.personal.ai.atlas.context.evidence.EvidenceType;
import com.campusguide.personal.ai.atlas.context.evidence.RetrievalEvidence;
import com.campusguide.personal.ai.atlas.context.query.QueryContext;
import com.campusguide.personal.ai.atlas.context.query.QueryDomain;
import com.campusguide.personal.ai.atlas.context.query.QueryIntent;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactEmbedding;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactIdentifier;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactMetadata;
import com.campusguide.personal.ai.atlas.knowledge.artifact.ArtifactSource;
import com.campusguide.personal.ai.atlas.knowledge.artifact.KnowledgeArtifact;
import com.campusguide.personal.ai.atlas.knowledge.citation.CitationGenerator;
import com.campusguide.personal.ai.atlas.knowledge.collection.KnowledgeCollectionRegistry;
import com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingService;
import com.campusguide.personal.ai.atlas.knowledge.embedding.MockEmbeddingProvider;
import com.campusguide.personal.ai.atlas.knowledge.ranking.ArtifactRankingService;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.KnowledgeRetrievalEngine.KnowledgeRetrievalResult;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.collection.CollectionSelector;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.hybrid.HybridRankingEngine;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.hybrid.HybridRetriever;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.semantic.SemanticRetriever;
import com.campusguide.personal.ai.atlas.knowledge.retrieval.semantic.VectorRetriever;
import com.campusguide.personal.ai.atlas.knowledge.vector.InMemoryVectorStore;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeRetrievalEngineTest {

    private KnowledgeRetrievalEngine retrievalEngine;
    private VectorRepository vectorRepository;
    private MockEmbeddingProvider mockEmbeddingProvider;
    private KnowledgeCollectionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new KnowledgeCollectionRegistry();
        CollectionSelector selector = new CollectionSelector(registry);

        InMemoryVectorStore store = new InMemoryVectorStore();
        mockEmbeddingProvider = new MockEmbeddingProvider();
        EmbeddingService embeddingService = new EmbeddingService(List.of(mockEmbeddingProvider), null);
        vectorRepository = new VectorRepository(store, embeddingService);
        VectorRetriever vectorRetriever = new VectorRetriever(store);
        SemanticRetriever semanticRetriever = new SemanticRetriever(vectorRetriever, embeddingService);

        HybridRankingEngine hybridRankingEngine = new HybridRankingEngine();
        HybridRetriever hybridRetriever = new HybridRetriever(semanticRetriever, vectorRepository, hybridRankingEngine);

        ArtifactRankingService rankingService = new ArtifactRankingService(registry);
        CitationGenerator citationGenerator = new CitationGenerator();

        retrievalEngine = new KnowledgeRetrievalEngine(
                selector, hybridRetriever, rankingService, citationGenerator, null);
    }

    @Test
    @DisplayName("Should execute complete collection-aware hybrid retrieval pipeline emitting RetrievalEvidence for Context Intelligence")
    void testEndToEndKnowledgeRetrievalPipeline() {
        String text = "The Student Services Center provides financial aid counseling and registration assistance.";
        ArtifactEmbedding emb = mockEmbeddingProvider.embed(com.campusguide.personal.ai.atlas.knowledge.embedding.EmbeddingRequest.of(text)).getEmbeddings().get(0);

        KnowledgeArtifact artifact = KnowledgeArtifact.builder()
                .id(ArtifactIdentifier.of("art_services_01"))
                .content(text)
                .collectionId(KnowledgeCollectionRegistry.PUBLIC_CAMPUS_KNOWLEDGE)
                .metadata(ArtifactMetadata.builder().category("campus_info").domain("campus").build())
                .source(ArtifactSource.builder().title("Student Services Handbook").sourceType("pdf").build())
                .embedding(emb)
                .build();

        vectorRepository.saveArtifact(artifact);

        QueryContext queryContext = QueryContext.builder()
                .rawQuery(text)
                .intent(QueryIntent.CAMPUS_NAVIGATION)
                .domainClassification(QueryDomain.CAMPUS)
                .confidenceScore(0.92)
                .build();

        KnowledgeRetrievalResult result = retrievalEngine.executeRetrieval(
                queryContext, "user_100", List.of("STUDENT"), null, null, 5, 0.0, null);

        assertFalse(result.artifacts().isEmpty());
        assertEquals(1, result.artifacts().size());
        assertEquals("art_services_01", result.artifacts().get(0).getId().getValue());

        assertFalse(result.citations().isEmpty());
        assertEquals("[1]", result.citations().get(0).getCitationMark());

        assertFalse(result.evidences().isEmpty());
        RetrievalEvidence ev = result.evidences().get(0);
        assertEquals(EvidenceType.RAG, ev.getType());
        assertTrue(ev.getContentSnippet().startsWith("[1]"));
        assertEquals("public_campus_knowledge", ev.getMetadata().get("collectionId"));
    }
}
