package com.electricistacat.backend.web.dto;

import com.electricistacat.backend.domain.enums.ChecklistStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChecklistItemRequest {
    @NotBlank
    private String description;

    @NotNull
    private ChecklistStatus status;

    private String evidenceUrl;

    private String notes;
}
