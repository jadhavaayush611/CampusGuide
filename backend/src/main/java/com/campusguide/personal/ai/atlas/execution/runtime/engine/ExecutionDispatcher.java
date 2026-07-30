package com.campusguide.personal.ai.atlas.execution.runtime.engine;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionStage;
import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.runtime.events.EventPublisher;
import com.campusguide.personal.ai.atlas.execution.runtime.events.ExecutionEvent;
import com.campusguide.personal.ai.atlas.execution.runtime.tool.ToolExecutor;
import com.campusguide.personal.ai.atlas.execution.runtime.tool.ToolResult;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ExecutionDispatcher handles dispatching and executing execution units sequentially or concurrently.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionDispatcher {

    private final ToolExecutor toolExecutor;
    private final EventPublisher eventPublisher;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public Map<String, ToolResult> dispatchUnits(WorkflowInstance instance, ExecutionStage stage) {
        Map<String, ToolResult> results = new ConcurrentHashMap<>();
        if (instance == null || stage == null || stage.getExecutionUnits() == null || stage.getExecutionUnits().isEmpty()) {
            return results;
        }

        List<ExecutionUnit> units = stage.getExecutionUnits();
        ExecutionContext context = instance.getContext();
        String workflowId = instance.getWorkflowId();

        if (stage.isParallel() && units.size() > 1) {
            log.info("Dispatching {} execution units in PARALLEL for stage {}", units.size(), stage.getStageId());
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (ExecutionUnit unit : units) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    ToolResult res = executeSingleUnit(instance, context, unit, workflowId, stage.getStageId());
                    results.put(unit.getUnitId(), res);
                }, executorService);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } else {
            log.info("Dispatching {} execution units SEQUENTIALLY for stage {}", units.size(), stage.getStageId());
            for (ExecutionUnit unit : units) {
                ToolResult res = executeSingleUnit(instance, context, unit, workflowId, stage.getStageId());
                results.put(unit.getUnitId(), res);
                if (res != null && res.getStatus() != null && !res.getStatus().isSuccess() && unit.isMandatory()) {
                    log.warn("Mandatory unit {} failed in stage {}, halting stage sequential execution", unit.getUnitId(), stage.getStageId());
                    break;
                }
            }
        }

        return results;
    }

    private ToolResult executeSingleUnit(WorkflowInstance instance, ExecutionContext context, ExecutionUnit unit, String workflowId, String stageId) {
        log.debug("Dispatching unit {} (capability: {})", unit.getUnitId(), unit.getTargetCapability());

        Map<String, Object> details = new HashMap<>();
        details.put("capability", unit.getTargetCapability());
        eventPublisher.publishExecutionEvent(ExecutionEvent.builder()
                .workflowId(workflowId)
                .instanceId(instance.getInstanceId())
                .stageId(stageId)
                .unitId(unit.getUnitId())
                .eventType("UNIT_START")
                .details(details)
                .build());

        ToolResult result = toolExecutor.executeUnit(context, unit, workflowId);

        instance.getSession().recordUnitResult(unit.getUnitId(), result);

        Map<String, Object> completeDetails = new HashMap<>(details);
        completeDetails.put("status", result.getStatus() != null ? result.getStatus().name() : "UNKNOWN");
        eventPublisher.publishExecutionEvent(ExecutionEvent.builder()
                .workflowId(workflowId)
                .instanceId(instance.getInstanceId())
                .stageId(stageId)
                .unitId(unit.getUnitId())
                .eventType("UNIT_EXECUTED")
                .details(completeDetails)
                .build());

        return result;
    }
}
