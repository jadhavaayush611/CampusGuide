package com.campusguide.personal.ai.atlas.prompt.instruction;

import org.springframework.stereotype.Component;

@Component
public class FormattingInstruction implements InstructionLayer {
    @Override
    public String getLayerName() {
        return "Formatting";
    }

    @Override
    public int getOrder() {
        return 40;
    }

    @Override
    public String renderInstruction() {
        return "Use clean Markdown formatting. Use bullet points for lists, bold text for key metrics or headers, and keep paragraphs clear and readable.";
    }
}
