package com.campusguide.personal.ai.atlas.context.evidence;

/**
 * Enumeration of supported evidence types for context retrieval.
 * Supports current direct/heuristic strategies and future RAG, vector, SQL, and memory sources.
 */
public enum EvidenceType {
    SQL,
    VECTOR,
    RAG,
    MEMORY,
    EXTERNAL_API,
    DIRECT,
    HEURISTIC,
    KEYWORD,
    RULE_BASED,
    DOMAIN_SERVICE,
    CAMPUS_KNOWLEDGE
}
