package com.campusguide.personal.ai.atlas.knowledge.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Historical record of a version update or lifecycle state transition for a KnowledgeCollection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionUpdateRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private String version;
    private Instant timestamp;
    private String description;
    private String updatedBy;
    private CollectionLifecycleState state;
}
