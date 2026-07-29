package com.campusguide.personal.ai.atlas.context.evidence;

/**
 * Enumeration of evidence sources detailing where retrieved context originated.
 */
public enum EvidenceSource {
    DATABASE(100),
    VECTOR_DB(95),
    CAMPUS_SERVICE(90),
    ACADEMIC_SERVICE(85),
    KNOWLEDGE_BASE(80),
    MEMORY_STORE(75),
    CACHE(70),
    DOMAIN_CONTRIBUTOR(65),
    EXTERNAL_SERVICE(60),
    HEURISTIC(50);

    private final int priorityWeight;

    EvidenceSource(int priorityWeight) {
        this.priorityWeight = priorityWeight;
    }

    public int getPriorityWeight() {
        return priorityWeight;
    }
}
