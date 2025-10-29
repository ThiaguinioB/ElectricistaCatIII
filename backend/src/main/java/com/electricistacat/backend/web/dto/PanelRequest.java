package com.electricistacat.backend.web.dto;

import com.electricistacat.backend.domain.enums.PanelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PanelRequest {
    @NotBlank
    private String name;

    @NotNull
    private PanelType panelType;

    private Integer dinRailCount;

    private String supplySource;

    private String notes;
}
