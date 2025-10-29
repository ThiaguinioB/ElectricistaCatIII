package com.electricistacat.backend.standards.model;

import lombok.Data;

@Data
public class BreakerCatalogEntry {
    private String curve;
    private Double rating;
    private Double tolerance;
    private String standard;
}
