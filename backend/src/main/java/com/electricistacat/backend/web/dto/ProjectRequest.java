package com.electricistacat.backend.web.dto;

import com.electricistacat.backend.domain.enums.VoltageSystemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectRequest {
    @NotBlank
    private String name;

    private String clientName;

    private String address;

    private String description;

    @NotNull
    private VoltageSystemType voltageSystemType;

    private Double nominalVoltage;

    private Double cosPhi;

    private Double efficiency;

    private String calcProfileCode;
}
