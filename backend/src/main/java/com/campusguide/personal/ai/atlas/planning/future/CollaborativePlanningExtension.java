package com.campusguide.personal.ai.atlas.planning.future;

import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;

import java.util.List;

/**
 * SPI Extension interface for multi-party collaborative planning.
 */
public interface CollaborativePlanningExtension {

    ExecutionPlan resolveMultiPartyPlan(ExecutionPlan basePlan, List<String> partyIds);
}
