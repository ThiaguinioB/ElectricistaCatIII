package com.electricistacat.backend.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class INRequest {
    @NotNull
    private Double designCurrent;

    private String preferredCurve;

    private Double tolerance;
}
