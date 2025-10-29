package com.electricistacat.backend.standards.model;

import lombok.Data;

@Data
public class CircuitTemplate {
    private String code;
    private String name;
    private String description;
    private Double defaultPowerKw;
    private TemplateBreaker recommendedBreaker;
    private TemplateCable recommendedCable;

    @Data
    public static class TemplateBreaker {
        private String curve;
        private Double rating;
    }

    @Data
    public static class TemplateCable {
        private Double crossSectionMm2;
        private String material;
        private String insulation;
    }
}
