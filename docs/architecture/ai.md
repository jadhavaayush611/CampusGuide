# Atlas AI Gateway & Knowledge Ingestion RAG Architecture

This document specifies the provider-independent Retrieval-Augmented Generation (RAG) knowledge ingestion and vector infrastructure for Atlas in CampusGuide.

---

## Architecture Overview

```
                      +-----------------------------+
                      | Raw Document Ingestion      |
                      | (PDF, DOCX, Markdown, TXT)  |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |      Document Parsers       |
                      | (Page, Section, Formatting) |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |   KnowledgeArtifact Engine  |
                      | (Universal Representation)  |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |       Chunking Engine       |
                      | (Fixed, Sliding, Semantic)  |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |   Embedding Infrastructure  |
                      |  (OpenAI, Local, Mock)      |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |   Vector Storage Engine     |
                      | (InMemory, pgvector, etc.)  |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |      Knowledge Catalog      |
                      |   (Registry & Diagnostics)  |
                      +-----------------------------+
```

---

## 1. KnowledgeArtifact Abstraction

The `KnowledgeArtifact` abstraction decouples Atlas from provider-specific data structures and models. All retrieval sources in Atlas produce and consume `KnowledgeArtifact`.

### Core Components
- **`KnowledgeArtifact`**: Universal model encapsulating normalized content, metadata, provenance, versioning, embeddings, references, lifecycle states, and retrieval hints.
- **`ArtifactMetadata`**: Flexible key-value attribute store tracking categories, size, domain, and custom tags.
- **`ArtifactIdentifier`**: Strongly-typed unique identifier wrapper (`art_...` for documents, `{parentId}_chk_{index}` for chunks).
- **`ArtifactType`**: Enum covering `DOCUMENT`, `CHUNK`, `PDF`, `DOCX`, `MARKDOWN`, `TXT`, `HTML`, `OCR`, `CSV`, `URL`, `SECTION`.
- **`ArtifactSource`**: Provenance tracking origin URI, mime type, title, author, line ranges, offsets, and page numbers.
- **`ArtifactVersion`**: Version string, revision counter, timestamp, and SHA-256 content checksum.
- **`ArtifactEmbedding`**: Vector array (`float[]`), provider name, model identifier, dimension, and creation timestamp.
- **`ArtifactReference`**: Encapsulates links between parent documents, child chunks, previous/next sequential chunks, and cross-references.

---

## 2. Ingestion Pipeline & Parser Abstraction

The ingestion pipeline converts raw inputs into indexed `KnowledgeArtifact`s.

### Pipeline Stages
1. **Document Loading**: `DocumentLoader` loads raw inputs into `RawDocument` containers and calculates SHA-256 checksums.
2. **Parser Dispatch**: `DocumentParser` implementations extract structural content:
   - `PdfDocumentParser`: Page boundary detection (`--- Page X ---`), heading hierarchy, metadata.
   - `DocxDocumentParser`: Paragraphs, headings, XML text token extraction.
   - `MarkdownDocumentParser`: Heading levels (`#`, `##`), frontmatter YAML metadata, formatting boundary preservation.
   - `TextDocumentParser`: Plain text normalization, paragraph structure, heading detection.
3. **Extraction & Building**: `ContentExtractor` sanitizes control characters and normalizes whitespace. `MetadataExtractor` constructs source provenance. `ArtifactBuilder` produces initialized `KnowledgeArtifact` instances.

---

## 3. Chunking Engine

The `ChunkingEngine` breaks parent `KnowledgeArtifact`s into child chunk `KnowledgeArtifact`s while propagating provenance and sequential references.

### Strategies
- **`FixedSizeChunker`**: Splits content into fixed character/token windows with configurable overlap.
- **`SlidingWindowChunker`**: Operates across word tokens with configurable window and step sizes.
- **`SemanticChunker`**: Groups content by paragraph/section/sentence boundaries without splitting mid-heading or mid-sentence, propagating section headings to chunk metadata.

---

## 4. Embedding Infrastructure

Provides provider-independent vector generation with caching, batching, and retries.

- **`EmbeddingService`**: Manages batch partitioning, in-memory checksum caching, exponential retries, and provider dispatches.
- **`OpenAIEmbeddingProvider`**: Connects to OpenAI embedding API (`text-embedding-3-small` / `text-embedding-3-large`).
- **`MockEmbeddingProvider`**: Produces deterministic float[] vectors for offline execution and fast testing.
- **`LocalEmbeddingProvider`**: Interface for local ONNX/DJL/Transformers models.

---

## 5. Vector Storage & Indexing

- **`VectorStore`**: Provider-independent interface for indexing, deleting, getting, and searching vector records.
- **`InMemoryVectorStore`**: Thread-safe in-memory vector database calculating Cosine Similarity and filtering by metadata.
- **`VectorRepository`**: High-level repository operating directly on `KnowledgeArtifact` models.
- **Backend Extension Points**: Pre-designed interface contracts for future `pgvector`, `Pinecone`, `Qdrant`, `Weaviate`, and `Milvus` providers.

---

## 6. Knowledge Catalog & Artifact Lifecycle

### Lifecycle States
- `DISCOVERED`: Raw document detected.
- `INGESTING`: Processing initiated.
- `PARSED`: Parsed by document parser.
- `CHUNKED`: Split into chunk artifacts.
- `EMBEDDED`: Vectors generated.
- `INDEXED`: Stored in vector database.
- `FAILED`: Failure encountered with diagnostic trace.
- `ARCHIVED`: Deprecated or soft-deleted.

### Catalog Management
- **`KnowledgeCatalog`**: Provides statistical summaries (`totalDocuments`, `totalChunks`, `totalTokens`, status counts).
- **`KnowledgeRegistry`**: Thread-safe registry tracking checksums to prevent duplicate re-indexing.

---

## 7. Extension Points

1. **Custom Document Parsers**: Implement `DocumentParser` and register as a Spring `@Component`.
2. **Custom Chunkers**: Implement `ChunkingStrategy` and add to `ChunkingEngine`.
3. **Vector Database Backends**: Implement `VectorStore` (e.g. `PgVectorStore`, `PineconeVectorStore`).
4. **Local Embedding Models**: Implement `LocalEmbeddingProvider`.
