package com.electricistacat.backend.standards.model;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CalcProfileDefinition {
    private String code;
    private String name;
    private String description;
    private List<String> standardReferences;
    private Map<String, Double> voltageDropLimits;
}
