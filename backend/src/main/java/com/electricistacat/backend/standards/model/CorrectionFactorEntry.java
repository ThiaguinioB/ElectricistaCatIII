package com.electricistacat.backend.standards.model;

import lombok.Data;

@Data
public class CorrectionFactorEntry {
    private String type;
    private String key;
    private Double factor;
    private String notes;
}
