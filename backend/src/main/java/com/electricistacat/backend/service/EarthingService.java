package com.electricistacat.backend.service;

import com.electricistacat.backend.domain.model.EarthingSystem;
import com.electricistacat.backend.domain.model.Measurement;
import com.electricistacat.backend.domain.model.Project;
import com.electricistacat.backend.repository.EarthingSystemRepository;
import com.electricistacat.backend.repository.MeasurementRepository;
import com.electricistacat.backend.web.dto.EarthingMeasurementRequest;
import com.electricistacat.backend.web.dto.MeasurementResponse;
import com.electricistacat.backend.web.mapper.MeasurementMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class EarthingService {

    private final EarthingSystemRepository earthingSystemRepository;
    private final MeasurementRepository measurementRepository;
    private final MeasurementMapper measurementMapper;
    private final ProjectService projectService;

    public EarthingService(
            EarthingSystemRepository earthingSystemRepository,
            MeasurementRepository measurementRepository,
            MeasurementMapper measurementMapper,
            ProjectService projectService) {
        this.earthingSystemRepository = earthingSystemRepository;
        this.measurementRepository = measurementRepository;
        this.measurementMapper = measurementMapper;
        this.projectService = projectService;
    }

    public MeasurementResponse logMeasurement(Long projectId, EarthingMeasurementRequest request) {
        EarthingSystem system = getOrCreateSystem(projectId);
        Measurement measurement = measurementMapper.toEntity(request);
        measurement.setEarthingSystem(system);
        Measurement saved = measurementRepository.save(measurement);
        if (system.getTargetResistance() != null
                && request.getResistance() > system.getTargetResistance()) {
            system.setImprovementActions(
                    "Resistencia por encima del objetivo. Considere aumentar longitud de la jabalina o usar humectantes.");
            earthingSystemRepository.save(system);
        }
        return measurementMapper.toResponse(saved);
    }

    public List<MeasurementResponse> listMeasurements(Long projectId) {
        EarthingSystem system = getOrCreateSystem(projectId);
        return measurementRepository.findByEarthingSystemId(system.getId()).stream()
                .map(measurementMapper::toResponse)
                .collect(Collectors.toList());
    }

    public EarthingSystem getOrCreateSystem(Long projectId) {
        Project project = projectService.loadProject(projectId);
        return earthingSystemRepository
                .findByProjectId(project.getId())
                .orElseGet(() -> {
                    EarthingSystem system = new EarthingSystem();
                    system.setProject(project);
                    system.setConfiguration("TT - Pozos con jabalina");
                    system.setTargetResistance(10.0);
                    system.setImprovementActions("Registrar mediciones iniciales y humedecer terreno si es necesario");
                    return earthingSystemRepository.save(system);
                });
    }
}
