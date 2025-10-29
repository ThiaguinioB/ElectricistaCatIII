package com.electricistacat.backend.web.mapper;

import com.electricistacat.backend.domain.model.Panel;
import com.electricistacat.backend.web.dto.PanelRequest;
import com.electricistacat.backend.web.dto.PanelResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PanelMapper {

    @Mapping(target = "project", ignore = true)
    Panel toEntity(PanelRequest request);

    PanelResponse toResponse(Panel panel);
}
