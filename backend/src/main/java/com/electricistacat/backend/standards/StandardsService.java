package com.electricistacat.backend.standards;

import com.electricistacat.backend.standards.model.BreakerCatalogEntry;
import com.electricistacat.backend.standards.model.CalcProfileDefinition;
import com.electricistacat.backend.standards.model.CircuitTemplate;
import com.electricistacat.backend.standards.model.ConductorProperties;
import com.electricistacat.backend.standards.model.CorrectionFactorEntry;
import com.electricistacat.backend.standards.model.StandardsDataset;
import com.electricistacat.backend.standards.model.ThermalCapacityEntry;
import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StandardsService {

    private final StandardsDatasetLoader datasetLoader;
    private StandardsDataset dataset;

    public StandardsService(StandardsDatasetLoader datasetLoader) {
        this.datasetLoader = datasetLoader;
    }

    @PostConstruct
    public void init() {
        this.dataset = datasetLoader.load();
    }

    public List<CalcProfileDefinition> getProfiles() {
        return dataset.getCalcProfiles();
    }

    public Optional<CalcProfileDefinition> getProfileByCode(String code) {
        return dataset.getCalcProfiles().stream()
                .filter(profile -> profile.getCode().equalsIgnoreCase(code))
                .findFirst();
    }

    public Optional<ThermalCapacityEntry> findThermalCapacity(
            String installationMethod, String material, String insulation, Double crossSection) {
        return dataset.getThermalCapacities().stream()
                .filter(entry -> entry.getInstallationMethod().equalsIgnoreCase(installationMethod))
                .filter(entry -> entry.getMaterial().equalsIgnoreCase(material))
                .filter(entry -> insulation == null
                        || insulation.equalsIgnoreCase(entry.getInsulation()))
                .filter(entry -> entry.getCrossSectionMm2().equals(crossSection))
                .min(Comparator.comparing(ThermalCapacityEntry::getStandard));
    }

    public double computeCorrectionFactor(String type, String key, double defaultFactor) {
        return dataset.getCorrectionFactors().stream()
                .filter(entry -> entry.getType().equalsIgnoreCase(type))
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(CorrectionFactorEntry::getFactor)
                .findFirst()
                .orElse(defaultFactor);
    }

    public double computeGroupingFactor(Integer groupingCount) {
        if (groupingCount == null || groupingCount <= 1) {
            return 1.0d;
        }
        String key = String.valueOf(groupingCount);
        return computeCorrectionFactor("grouping", key, 1.0d);
    }

    public double computeAmbientFactor(Double ambientTemperature) {
        if (ambientTemperature == null) {
            return 1.0d;
        }
        String key = String.valueOf(ambientTemperature.intValue());
        return computeCorrectionFactor("ambient_temperature", key, 1.0d);
    }

    public Optional<BreakerCatalogEntry> findBreaker(double designCurrent, String preferredCurve, Double tolerance) {
        double requestedTolerance = tolerance != null ? tolerance : 1.0d;
        return dataset.getBreakerCatalog().stream()
                .filter(entry -> preferredCurve == null
                        || entry.getCurve().equalsIgnoreCase(preferredCurve))
                .filter(entry -> entry.getRating()
                        * (tolerance != null ? tolerance : entry.getTolerance())
                        >= designCurrent)
                .sorted(Comparator.comparing(BreakerCatalogEntry::getRating))
                .filter(entry -> entry.getRating() * requestedTolerance >= designCurrent)
                .findFirst();
    }

    public Optional<ConductorProperties> findConductor(String material) {
        if (material == null) {
            return Optional.empty();
        }
        return dataset.getConductorProperties().stream()
                .filter(entry -> entry.getMaterial().equalsIgnoreCase(material))
                .findFirst();
    }

    public Map<String, CircuitTemplate> getTemplatesByCode() {
        return dataset.getTemplates().stream()
                .collect(Collectors.toMap(CircuitTemplate::getCode, template -> template));
    }

    public List<CircuitTemplate> listTemplates() {
        return dataset.getTemplates();
    }
}
