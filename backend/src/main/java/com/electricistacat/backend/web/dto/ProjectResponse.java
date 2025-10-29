package com.electricistacat.backend.web.dto;

import com.electricistacat.backend.domain.enums.VoltageSystemType;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProjectResponse {
    Long id;
    String name;
    String clientName;
    String address;
    String description;
    VoltageSystemType voltageSystemType;
    Double nominalVoltage;
    Double cosPhi;
    Double efficiency;
    String calcProfileCode;
    Instant createdAt;
    Instant updatedAt;
}
