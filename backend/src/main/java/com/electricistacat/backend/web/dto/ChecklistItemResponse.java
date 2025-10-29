package com.electricistacat.backend.web.dto;

import com.electricistacat.backend.domain.enums.ChecklistStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChecklistItemResponse {
    Long id;
    String description;
    ChecklistStatus status;
    String evidenceUrl;
    String notes;
}
