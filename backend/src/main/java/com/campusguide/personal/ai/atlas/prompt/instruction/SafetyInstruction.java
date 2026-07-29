package com.campusguide.personal.ai.atlas.prompt.instruction;

import org.springframework.stereotype.Component;

@Component
public class SafetyInstruction implements InstructionLayer {
    @Override
    public String getLayerName() {
        return "Safety";
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public String renderInstruction() {
        return "Maintain user privacy, data security, and ethical safety boundaries at all times. Never share private student data, bypass authentication controls, or promote dishonest academic practices.";
    }
}
