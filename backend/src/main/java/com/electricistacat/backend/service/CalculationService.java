package com.electricistacat.backend.service;

import com.electricistacat.backend.config.AppDefaultsProperties;
import com.electricistacat.backend.standards.StandardsService;
import com.electricistacat.backend.standards.model.BreakerCatalogEntry;
import com.electricistacat.backend.standards.model.CalcProfileDefinition;
import com.electricistacat.backend.standards.model.ConductorProperties;
import com.electricistacat.backend.standards.model.ThermalCapacityEntry;
import com.electricistacat.backend.web.dto.CalculationResponse;
import com.electricistacat.backend.web.dto.IARequest;
import com.electricistacat.backend.web.dto.INRequest;
import com.electricistacat.backend.web.dto.IZRequest;
import com.electricistacat.backend.web.dto.VoltageDropRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CalculationService {

    private final StandardsService standardsService;
    private final AppDefaultsProperties properties;

    public CalculationService(StandardsService standardsService, AppDefaultsProperties properties) {
        this.standardsService = standardsService;
        this.properties = properties;
    }

    public CalculationResponse calculateIA(IARequest request) {
        double voltage = Optional.ofNullable(request.getVoltage())
                .orElse(request.getThreePhase() != null && request.getThreePhase()
                        ? properties.getDefault().getVoltageThreePhase()
                        : properties.getDefault().getVoltageSinglePhase());
        double cosPhi = Optional.ofNullable(request.getPowerFactor())
                .orElse(properties.getDefault().getCosphi());
        double efficiency = Optional.ofNullable(request.getEfficiency())
                .orElse(properties.getDefault().getEfficiency());
        double powerW = request.getPowerKw() * 1000d;
        double denominator = (request.getThreePhase() != null && request.getThreePhase())
                ? Math.sqrt(3) * voltage * cosPhi * efficiency
                : voltage * cosPhi * efficiency;
        double ia = powerW / denominator;
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("voltage", voltage);
        metadata.put("cosPhi", cosPhi);
        metadata.put("efficiency", efficiency);
        return CalculationResponse.builder().result(ia).unit("A").metadata(metadata).build();
    }

    public CalculationResponse calculateIN(INRequest request) {
        BreakerCatalogEntry breaker = standardsService
                .findBreaker(request.getDesignCurrent(), request.getPreferredCurve(), request.getTolerance())
                .orElseThrow(() -> new IllegalArgumentException("No breaker found for design current"));
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("curve", breaker.getCurve());
        metadata.put("rating", breaker.getRating());
        metadata.put("tolerance", breaker.getTolerance());
        metadata.put("standard", breaker.getStandard());
        return CalculationResponse.builder()
                .result(breaker.getRating())
                .unit("A")
                .metadata(metadata)
                .build();
    }

    public CalculationResponse calculateIZ(IZRequest request) {
        String material = Optional.ofNullable(request.getMaterial())
                .orElse(properties.getDefault().getConductorMaterial());
        String insulation = request.getInsulation();
        ThermalCapacityEntry entry = standardsService
                .findThermalCapacity(request.getInstallationMethod(), material, insulation, request.getCrossSectionMm2())
                .orElseThrow(() -> new IllegalArgumentException("No IZ data for parameters"));
        double ambientFactor = standardsService.computeAmbientFactor(request.getAmbientTemperature());
        double groupingFactor = standardsService.computeGroupingFactor(request.getGroupingCount());
        double izCorrected = entry.getIz() * ambientFactor * groupingFactor;
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("baseIz", entry.getIz());
        metadata.put("ambientFactor", ambientFactor);
        metadata.put("groupingFactor", groupingFactor);
        metadata.put("standard", entry.getStandard());
        return CalculationResponse.builder()
                .result(izCorrected)
                .unit("A")
                .metadata(metadata)
                .build();
    }

    public CalculationResponse calculateVoltageDrop(VoltageDropRequest request) {
        double current = request.getCurrent();
        double length = request.getLengthMeters();
        String material = Optional.ofNullable(request.getMaterial())
                .orElse(properties.getDefault().getConductorMaterial());
        double voltage = Optional.ofNullable(request.getVoltage())
                .orElse(request.getThreePhase() != null && request.getThreePhase()
                        ? properties.getDefault().getVoltageThreePhase()
                        : properties.getDefault().getVoltageSinglePhase());
        double powerFactor = Optional.ofNullable(request.getPowerFactor())
                .orElse(properties.getDefault().getCosphi());
        ConductorProperties conductor = standardsService
                .findConductor(material)
                .orElseThrow(() -> new IllegalArgumentException("No conductor properties for material"));
        double resistancePerKm = conductor.getResistivityOhmPerKm() / request.getCrossSectionMm2();
        double reactancePerKm = conductor.getReactanceOhmPerKm();
        double lengthKm = length / 1000d;
        double multiplier = (request.getThreePhase() != null && request.getThreePhase()) ? Math.sqrt(3) : 2.0d;
        double voltageDrop = multiplier * current * lengthKm
                * (resistancePerKm * powerFactor + reactancePerKm * Math.sqrt(1 - Math.pow(powerFactor, 2)));
        double percent = (voltageDrop / voltage) * 100d;
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("material", material);
        metadata.put("resistancePerKm", resistancePerKm);
        metadata.put("reactancePerKm", reactancePerKm);
        metadata.put("percent", percent);
        metadata.put("voltage", voltage);
        return CalculationResponse.builder()
                .result(voltageDrop)
                .unit("V")
                .metadata(metadata)
                .build();
    }

    public double resolveVoltageDropLimit(boolean lighting, CalcProfileDefinition profile) {
        if (profile != null && profile.getVoltageDropLimits() != null) {
            String key = lighting ? "lighting" : "general";
            Double limit = profile.getVoltageDropLimits().get(key);
            if (limit != null) {
                return limit;
            }
        }
        return lighting
                ? properties.getVoltageDrop().getLightingLimitPercent()
                : properties.getVoltageDrop().getGeneralLimitPercent();
    }
}
