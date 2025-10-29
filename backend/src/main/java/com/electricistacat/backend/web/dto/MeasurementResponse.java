package com.electricistacat.backend.web.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MeasurementResponse {
    Long id;
    Double resistance;
    Instant measuredAt;
    String instrument;
    String notes;
}
