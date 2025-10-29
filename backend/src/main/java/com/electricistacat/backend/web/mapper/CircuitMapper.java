package com.electricistacat.backend.web.mapper;

import com.electricistacat.backend.domain.model.Circuit;
import com.electricistacat.backend.web.dto.CircuitRequest;
import com.electricistacat.backend.web.dto.CircuitResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CircuitMapper {

    @Mapping(target = "panel", ignore = true)
    Circuit toEntity(CircuitRequest request);

    CircuitResponse toResponse(Circuit circuit);
}
