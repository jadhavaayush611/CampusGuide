package com.campusguide.personal.ai.atlas.execution.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry managing workflow templates and registered templates.
 */
@Slf4j
@Component
public class WorkflowRegistry {

    private final Map<String, WorkflowTemplate> templates = new ConcurrentHashMap<>();

    public WorkflowRegistry() {
        registerTemplate(WorkflowTemplate.defaultTemplate());
    }

    public void registerTemplate(WorkflowTemplate template) {
        if (template != null && template.getTemplateId() != null) {
            templates.put(template.getTemplateId(), template);
            log.debug("Registered WorkflowTemplate: id={}, name={}", template.getTemplateId(), template.getTemplateName());
        }
    }

    public Optional<WorkflowTemplate> getTemplate(String templateId) {
        return Optional.ofNullable(templates.get(templateId));
    }

    public boolean hasTemplate(String templateId) {
        return templates.containsKey(templateId);
    }

    public Map<String, WorkflowTemplate> getAllTemplates() {
        return new ConcurrentHashMap<>(templates);
    }
}
