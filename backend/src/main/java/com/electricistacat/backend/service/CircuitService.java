package com.electricistacat.backend.service;

import com.electricistacat.backend.config.AppDefaultsProperties;
import com.electricistacat.backend.domain.model.Circuit;
import com.electricistacat.backend.domain.model.Material;
import com.electricistacat.backend.domain.model.Panel;
import com.electricistacat.backend.domain.model.Project;
import com.electricistacat.backend.repository.CircuitRepository;
import com.electricistacat.backend.repository.MaterialRepository;
import com.electricistacat.backend.standards.StandardsService;
import com.electricistacat.backend.standards.model.CalcProfileDefinition;
import com.electricistacat.backend.web.dto.CalculationResponse;
import com.electricistacat.backend.web.dto.CircuitRequest;
import com.electricistacat.backend.web.dto.CircuitResponse;
import com.electricistacat.backend.web.dto.IARequest;
import com.electricistacat.backend.web.dto.INRequest;
import com.electricistacat.backend.web.dto.IZRequest;
import com.electricistacat.backend.web.dto.VoltageDropRequest;
import com.electricistacat.backend.web.error.ResourceNotFoundException;
import com.electricistacat.backend.web.mapper.CircuitMapper;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CircuitService {

    private final CircuitRepository circuitRepository;
    private final CircuitMapper circuitMapper;
    private final PanelService panelService;
    private final CalculationService calculationService;
    private final AppDefaultsProperties defaultsProperties;
    private final MaterialRepository materialRepository;
    private final StandardsService standardsService;

    public CircuitService(
            CircuitRepository circuitRepository,
            CircuitMapper circuitMapper,
            PanelService panelService,
            CalculationService calculationService,
            AppDefaultsProperties defaultsProperties,
            MaterialRepository materialRepository,
            StandardsService standardsService) {
        this.circuitRepository = circuitRepository;
        this.circuitMapper = circuitMapper;
        this.panelService = panelService;
        this.calculationService = calculationService;
        this.defaultsProperties = defaultsProperties;
        this.materialRepository = materialRepository;
        this.standardsService = standardsService;
    }

    public CircuitResponse addCircuit(Long panelId, CircuitRequest request) {
        Panel panel = panelService.loadPanel(panelId);
        Project project = panel.getProject();
        Circuit circuit = circuitMapper.toEntity(request);
        circuit.setPanel(panel);

        double powerKw = Optional.ofNullable(request.getDemandPowerKw())
                .orElseThrow(() -> new IllegalArgumentException("La potencia demandada es obligatoria"));
        IARequest iaRequest = new IARequest();
        iaRequest.setPowerKw(powerKw);
        iaRequest.setVoltage(Optional.ofNullable(request.getVoltage()).orElse(project.getNominalVoltage()));
        iaRequest.setPowerFactor(Optional.ofNullable(request.getCosPhi()).orElse(project.getCosPhi()));
        iaRequest.setEfficiency(Optional.ofNullable(request.getEfficiency()).orElse(project.getEfficiency()));
        iaRequest.setThreePhase(request.getPhaseType().name().contains("THREE"));
        CalculationResponse ia = calculationService.calculateIA(iaRequest);
        circuit.setDesignCurrent(ia.getResult());

        INRequest inRequest = new INRequest();
        inRequest.setDesignCurrent(ia.getResult());
        inRequest.setPreferredCurve(request.getProtectiveDeviceCurve());
        CalculationResponse in = calculationService.calculateIN(inRequest);
        circuit.setBreakerCurrent(in.getResult());
        circuit.setProtectiveDeviceCurve((String) in.getMetadata().get("curve"));
        circuit.setProtectiveDeviceRating(in.getResult());

        String installationMethod = Optional.ofNullable(request.getInstallationMethod())
                .orElse(defaultsProperties.getDefault().getInstallationMethod());
        String material = Optional.ofNullable(request.getConductorMaterial())
                .orElse(defaultsProperties.getDefault().getConductorMaterial());
        IZRequest izRequest = new IZRequest();
        izRequest.setCrossSectionMm2(Optional.ofNullable(request.getConductorCrossSection())
                .orElseThrow(() -> new IllegalArgumentException("Se requiere sección del conductor")));
        izRequest.setInstallationMethod(installationMethod);
        izRequest.setMaterial(material);
        izRequest.setInsulation(request.getConductorInsulation());
        izRequest.setAmbientTemperature(request.getAmbientTemperature());
        izRequest.setGroupingCount(request.getGroupingFactorCount());
        CalculationResponse iz = calculationService.calculateIZ(izRequest);
        circuit.setIz(iz.getResult());
        if (in.getResult() > iz.getResult()) {
            throw new IllegalArgumentException("IN supera la capacidad térmica corregida IZ");
        }

        VoltageDropRequest vdRequest = new VoltageDropRequest();
        vdRequest.setCurrent(ia.getResult());
        vdRequest.setLengthMeters(Optional.ofNullable(request.getLengthMeters()).orElse(0d));
        vdRequest.setCrossSectionMm2(izRequest.getCrossSectionMm2());
        vdRequest.setMaterial(material);
        vdRequest.setVoltage(iaRequest.getVoltage());
        vdRequest.setThreePhase(iaRequest.getThreePhase());
        vdRequest.setPowerFactor(iaRequest.getPowerFactor());
        CalculationResponse vd = calculationService.calculateVoltageDrop(vdRequest);
        circuit.setVoltageDrop((Double) vd.getMetadata().get("percent"));

        CalcProfileDefinition calcProfileDefinition = null;
        if (project.getCalcProfile() != null) {
            calcProfileDefinition = standardsService
                    .getProfileByCode(project.getCalcProfile().getCode())
                    .orElse(null);
        }
        double limit = calculationService.resolveVoltageDropLimit(
                Boolean.TRUE.equals(request.getLighting()), calcProfileDefinition);
        if ((Double) vd.getMetadata().get("percent") > limit) {
            throw new IllegalArgumentException(
                    "La caída de tensión excede el límite permitido: " + limit + "%");
        }

        Circuit saved = circuitRepository.save(circuit);
        addBillOfMaterialsEntries(project, saved, in, izRequest.getCrossSectionMm2(), material);
        return circuitMapper.toResponse(saved);
    }

    public Circuit loadCircuit(Long id) {
        return circuitRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Circuito no encontrado"));
    }

    public java.util.List<CircuitResponse> listByPanel(Long panelId) {
        panelService.loadPanel(panelId);
        return circuitRepository.findByPanelId(panelId).stream()
                .map(circuitMapper::toResponse)
                .toList();
    }

    private void addBillOfMaterialsEntries(
            Project project, Circuit circuit, CalculationResponse breaker, Double crossSection, String material) {
        Material protective = new Material();
        protective.setProject(project);
        protective.setDescription("Interruptor curva " + breaker.getMetadata().get("curve"));
        protective.setUnit("pc");
        protective.setQuantity(1d);
        protective.setReferenceStandard((String) breaker.getMetadata().get("standard"));
        materialRepository.save(protective);

        Material cable = new Material();
        cable.setProject(project);
        cable.setDescription("Cable " + material + " " + crossSection + "mm2 para " + circuit.getName());
        cable.setUnit("m");
        cable.setQuantity(Optional.ofNullable(circuit.getLengthMeters()).orElse(0d));
        cable.setReferenceStandard("IRAM 2183");
        materialRepository.save(cable);
    }
}
