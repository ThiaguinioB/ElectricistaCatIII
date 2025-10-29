package com.electricistacat.backend.web.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CalculationResponse {
    Double result;
    String unit;
    Map<String, Object> metadata;
}
