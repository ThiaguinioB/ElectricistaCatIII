package com.electricistacat.backend.web.mapper;

import com.electricistacat.backend.domain.model.ChecklistItem;
import com.electricistacat.backend.web.dto.ChecklistItemRequest;
import com.electricistacat.backend.web.dto.ChecklistItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChecklistItemMapper {

    @Mapping(target = "project", ignore = true)
    ChecklistItem toEntity(ChecklistItemRequest request);

    ChecklistItemResponse toResponse(ChecklistItem item);
}
