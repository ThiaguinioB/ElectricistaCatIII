package com.electricistacat.backend.web.mapper;

import com.electricistacat.backend.domain.model.Material;
import com.electricistacat.backend.web.dto.BomItemResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MaterialMapper {
    BomItemResponse toResponse(Material material);
}
