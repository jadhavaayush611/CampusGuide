package com.campusguide.personal.ai.atlas.prompt.instruction;

import org.springframework.stereotype.Component;

@Component
public class ResponsePolicyInstruction implements InstructionLayer {
    @Override
    public String getLayerName() {
        return "ResponsePolicy";
    }

    @Override
    public int getOrder() {
        return 50;
    }

    @Override
    public String renderInstruction() {
        return "Be direct, concise, and helpful. Prioritize user clarity and actionability. If requested information is unknown or context is missing, acknowledge constraints politely.";
    }
}
