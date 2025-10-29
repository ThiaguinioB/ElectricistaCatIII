package com.electricistacat.backend.standards.model;

import lombok.Data;

@Data
public class ThermalCapacityEntry {
    private String standard;
    private String installationMethod;
    private String material;
    private String insulation;
    private Double crossSectionMm2;
    private Double iz;
}
