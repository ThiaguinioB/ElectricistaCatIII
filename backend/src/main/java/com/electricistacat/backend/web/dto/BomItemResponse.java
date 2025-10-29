package com.electricistacat.backend.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BomItemResponse {
    Long id;
    String description;
    String unit;
    Double quantity;
    Double unitCost;
    String referenceStandard;
    String supplier;
}
