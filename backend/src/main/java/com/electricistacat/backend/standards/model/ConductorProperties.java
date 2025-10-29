package com.electricistacat.backend.standards.model;

import lombok.Data;

@Data
public class ConductorProperties {
    private String material;
    private Double resistivityOhmPerKm;
    private Double reactanceOhmPerKm;
}
