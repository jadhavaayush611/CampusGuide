package com.campusguide.personal.ai.atlas.prompt.instruction;

/**
 * Interface representing an ordered system instruction component.
 */
public interface InstructionLayer {
    String getLayerName();
    int getOrder();
    String renderInstruction();
}
