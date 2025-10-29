package com.electricistacat.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IZRequest {
    @NotNull
    private Double crossSectionMm2;

    @NotBlank
    private String installationMethod;

    @NotBlank
    private String material;

    private String insulation;

    private Double ambientTemperature;

    private Integer groupingCount;
}
