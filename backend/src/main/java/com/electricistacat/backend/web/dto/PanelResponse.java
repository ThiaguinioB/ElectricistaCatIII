package com.electricistacat.backend.web.dto;

import com.electricistacat.backend.domain.enums.PanelType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PanelResponse {
    Long id;
    String name;
    PanelType panelType;
    Integer dinRailCount;
    String supplySource;
    String notes;
}
