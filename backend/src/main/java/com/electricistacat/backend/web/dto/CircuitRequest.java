package com.electricistacat.backend.web.dto;

import com.electricistacat.backend.domain.enums.PhaseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CircuitRequest {
    @NotBlank
    private String name;

    private String loadType;

    private Double demandPowerKw;

    private Double voltage;

    private Double cosPhi;

    private Double efficiency;

    @NotNull
    private PhaseType phaseType;

    private Double lengthMeters;

    private Double conductorCrossSection;

    private String conductorMaterial;

    private String conductorInsulation;

    private String installationMethod;

    private Integer groupingFactorCount;

    private Double ambientTemperature;

    private Boolean lighting;

    private String conductorColors;
}
