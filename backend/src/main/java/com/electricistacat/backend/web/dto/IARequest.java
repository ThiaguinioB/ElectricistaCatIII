package com.electricistacat.backend.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IARequest {
    @NotNull
    private Double powerKw;

    private Double voltage;

    private Double powerFactor;

    private Double efficiency;

    private Boolean threePhase;
}
