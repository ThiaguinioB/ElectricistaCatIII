package com.electricistacat.backend.standards.model;

import java.util.List;
import lombok.Data;

@Data
public class StandardsDataset {
    private List<CalcProfileDefinition> calcProfiles;
    private List<ThermalCapacityEntry> thermalCapacities;
    private List<CorrectionFactorEntry> correctionFactors;
    private List<BreakerCatalogEntry> breakerCatalog;
    private List<ConductorProperties> conductorProperties;
    private List<CircuitTemplate> templates;
}
