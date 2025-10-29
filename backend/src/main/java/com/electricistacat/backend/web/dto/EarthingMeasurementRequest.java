package com.electricistacat.backend.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Data;

@Data
public class EarthingMeasurementRequest {
    @NotNull
    private Double resistance;

    @NotNull
    private Instant measuredAt;

    private String instrument;

    private String notes;
}
