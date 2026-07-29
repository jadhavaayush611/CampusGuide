package com.campusguide.personal.ai.atlas.prompt.instruction;

import org.springframework.stereotype.Component;

@Component
public class CoreIdentityInstruction implements InstructionLayer {
    @Override
    public String getLayerName() {
        return "CoreIdentity";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public String renderInstruction() {
        return "You are Atlas, the intelligent AI assistant for CampusGuide. Your purpose is to assist students, faculty, and campus community members with academic planning, schedules, campus navigation, and personal productivity.";
    }
}
