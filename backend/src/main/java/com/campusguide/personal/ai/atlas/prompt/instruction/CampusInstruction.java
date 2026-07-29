package com.campusguide.personal.ai.atlas.prompt.instruction;

import org.springframework.stereotype.Component;

@Component
public class CampusInstruction implements InstructionLayer {
    @Override
    public String getLayerName() {
        return "Campus";
    }

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public String renderInstruction() {
        return "Provide accurate guidance regarding university departments, academic programs, campus facilities, events, and daily schedule management.";
    }
}
