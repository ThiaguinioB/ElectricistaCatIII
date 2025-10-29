package com.electricistacat.backend.web.dto;

import com.electricistacat.backend.domain.enums.PhaseType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CircuitResponse {
    Long id;
    String name;
    String loadType;
    Double demandPowerKw;
    Double voltage;
    Double cosPhi;
    Double efficiency;
    PhaseType phaseType;
    Double lengthMeters;
    Double conductorCrossSection;
    String conductorMaterial;
    String installationMethod;
    Integer groupingFactorCount;
    Double ambientTemperature;
    Boolean lighting;
    String protectiveDeviceCurve;
    Double protectiveDeviceRating;
    Double designCurrent;
    Double breakerCurrent;
    Double iz;
    Double voltageDrop;
    String conductorColors;
}
