package com.electricistacat.backend.web.mapper;

import com.electricistacat.backend.domain.model.Measurement;
import com.electricistacat.backend.web.dto.EarthingMeasurementRequest;
import com.electricistacat.backend.web.dto.MeasurementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MeasurementMapper {

    @Mapping(target = "earthingSystem", ignore = true)
    Measurement toEntity(EarthingMeasurementRequest request);

    MeasurementResponse toResponse(Measurement measurement);
}
