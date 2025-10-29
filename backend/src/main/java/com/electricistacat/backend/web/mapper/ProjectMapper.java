package com.electricistacat.backend.web.mapper;

import com.electricistacat.backend.domain.model.Project;
import com.electricistacat.backend.web.dto.ProjectRequest;
import com.electricistacat.backend.web.dto.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "calcProfile", ignore = true)
    Project toEntity(ProjectRequest request);

    @Mapping(target = "calcProfileCode", source = "calcProfile.code")
    ProjectResponse toResponse(Project project);
}
