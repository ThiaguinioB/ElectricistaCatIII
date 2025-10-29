package com.electricistacat.backend.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoltageDropRequest {
    @NotNull
    private Double current;

    @NotNull
    private Double lengthMeters;

    @NotNull
    private Double crossSectionMm2;

    private String material;

    private Double voltage;

    private Boolean threePhase;

    private Double powerFactor;
}
