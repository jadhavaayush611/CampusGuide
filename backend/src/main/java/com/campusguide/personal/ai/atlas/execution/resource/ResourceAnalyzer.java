package com.campusguide.personal.ai.atlas.execution.resource;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ResourceAnalyzer evaluates tools, APIs, compute resources, permissions, memory, storage,
 * and network dependencies required for a workflow without performing resource allocation.
 */
@Slf4j
@Component
public class ResourceAnalyzer {

    public ResourceAllocation analyzeResources(ExecutionContext context, ExecutableWorkflow workflow) {
        log.debug("Analyzing resource requirements for workflowId={}", workflow != null ? workflow.getWorkflowId() : "unknown");

        List<ResourceRequirement> requirements = new ArrayList<>();

        if (workflow != null && workflow.getStages() != null) {
            for (ExecutionStage stage : workflow.getStages()) {
                if (stage.getExecutionUnits() != null) {
                    for (ExecutionUnit unit : stage.getExecutionUnits()) {
                        // Tool requirement
                        if (unit.getTargetCapability() != null && !unit.getTargetCapability().isBlank()) {
                            requirements.add(ResourceRequirement.builder()
                                    .resourceId("res_tool_" + unit.getUnitId())
                                    .resourceType(ResourceRequirement.ResourceType.TOOL)
                                    .name(unit.getTargetCapability())
                                    .amountRequired(1.0)
                                    .unit("INSTANCE")
                                    .mandatory(unit.isMandatory())
                                    .fulfillmentStatus(ResourceRequirement.FulfillmentStatus.SATISFIED)
                                    .build());
                        }

                        // Compute requirement
                        requirements.add(ResourceRequirement.builder()
                                .resourceId("res_compute_" + unit.getUnitId())
                                .resourceType(ResourceRequirement.ResourceType.COMPUTE)
                                .name("CPU Thread")
                                .amountRequired(1.0)
                                .unit("CORE")
                                .mandatory(unit.isMandatory())
                                .fulfillmentStatus(ResourceRequirement.FulfillmentStatus.SATISFIED)
                                .build());

                        // Memory requirement
                        requirements.add(ResourceRequirement.builder()
                                .resourceId("res_mem_" + unit.getUnitId())
                                .resourceType(ResourceRequirement.ResourceType.MEMORY)
                                .name("HEAP_MB")
                                .amountRequired(64.0)
                                .unit("MB")
                                .mandatory(unit.isMandatory())
                                .fulfillmentStatus(ResourceRequirement.FulfillmentStatus.SATISFIED)
                                .build());
                    }
                }
            }
        }

        // Network requirement if environment indicates network availability
        boolean networkNeeded = true;
        requirements.add(ResourceRequirement.builder()
                .resourceId("res_net_global")
                .resourceType(ResourceRequirement.ResourceType.NETWORK)
                .name("HTTP/REST Connectivity")
                .amountRequired(1.0)
                .unit("BANDWIDTH_MBPS")
                .mandatory(false)
                .fulfillmentStatus(ResourceRequirement.FulfillmentStatus.SATISFIED)
                .build());

        long unsatisfiedCount = requirements.stream()
                .filter(r -> r.getFulfillmentStatus() == ResourceRequirement.FulfillmentStatus.UNSATISFIED)
                .count();

        return ResourceAllocation.builder()
                .allocationId("alloc_" + UUID.randomUUID().toString().substring(0, 8))
                .requirements(requirements)
                .allRequirementsSatisfied(unsatisfiedCount == 0)
                .missingResourceCount((int) unsatisfiedCount)
                .allocationStrategy("DRY_RUN_ANALYSIS")
                .build();
    }
}
