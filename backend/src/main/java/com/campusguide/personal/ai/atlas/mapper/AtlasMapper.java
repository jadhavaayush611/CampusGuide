package com.campusguide.personal.ai.atlas.mapper;

import com.campusguide.personal.ai.atlas.dto.AtlasChatMessageDto;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.dto.AtlasUsageDto;
import com.campusguide.personal.ai.atlas.model.AtlasChatMessage;
import com.campusguide.personal.ai.atlas.model.AtlasNormalizedResponse;
import com.campusguide.personal.ai.atlas.model.AtlasRole;
import com.campusguide.personal.ai.atlas.model.AtlasUsageInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AtlasMapper {

    public AtlasChatMessage toModel(AtlasChatMessageDto dto) {
        if (dto == null) {
            return null;
        }
        return AtlasChatMessage.builder()
                .role(AtlasRole.fromValue(dto.getRole()))
                .content(dto.getContent())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public List<AtlasChatMessage> toModelList(List<AtlasChatMessageDto> dtos) {
        if (dtos == null) {
            return new ArrayList<>();
        }
        return dtos.stream().map(this::toModel).toList();
    }

    public AtlasChatMessageDto toDto(AtlasChatMessage model) {
        if (model == null) {
            return null;
        }
        return AtlasChatMessageDto.builder()
                .role(model.getRole() != null ? model.getRole().getValue() : "user")
                .content(model.getContent())
                .build();
    }

    public AtlasUsageDto toDto(AtlasUsageInfo usageInfo) {
        if (usageInfo == null) {
            return AtlasUsageDto.builder().build();
        }
        return AtlasUsageDto.builder()
                .promptTokens(usageInfo.getPromptTokens())
                .completionTokens(usageInfo.getCompletionTokens())
                .totalTokens(usageInfo.getTotalTokens())
                .build();
    }

    public AtlasChatResponse toResponseDto(AtlasNormalizedResponse response) {
        if (response == null) {
            return null;
        }
        return AtlasChatResponse.builder()
                .id(response.getId())
                .content(response.getContent())
                .role(response.getRole() != null ? response.getRole().getValue() : "assistant")
                .model(response.getModelUsed())
                .finishReason(response.getFinishReason())
                .usage(toDto(response.getUsage()))
                .timestamp(response.getTimestamp())
                .metadata(response.getMetadata())
                .build();
    }
}
