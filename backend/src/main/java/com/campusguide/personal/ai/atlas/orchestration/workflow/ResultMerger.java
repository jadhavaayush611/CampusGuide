package com.campusguide.personal.ai.atlas.orchestration.workflow;

import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component for merging results from parallel/distributed workflow partitions back into unified workflow outputs.
 */
@Slf4j
@Component
public class ResultMerger {

    public Map<String, Object> mergePartitionResults(List<WorkflowPartition> partitions) {
        Map<String, Object> mergedResults = new HashMap<>();
        if (partitions == null) return mergedResults;

        for (WorkflowPartition partition : partitions) {
            if (partition.getResultData() != null) {
                log.debug("Merging results from partition {}", partition.getPartitionId());
                mergedResults.putAll(partition.getResultData());
            }
        }
        return mergedResults;
    }

    public void applyToInstance(WorkflowInstance instance, List<WorkflowPartition> partitions) {
        if (instance == null || partitions == null) return;
        Map<String, Object> merged = mergePartitionResults(partitions);
        if (instance.getSession() != null && instance.getSession().getVariables() != null) {
            instance.getSession().getVariables().putAll(merged);
        }
        log.info("Applied merged partition outputs to WorkflowInstance {}", instance.getInstanceId());
    }
}
