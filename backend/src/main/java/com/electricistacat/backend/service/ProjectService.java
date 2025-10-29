package com.electricistacat.backend.service;

import com.electricistacat.backend.config.AppDefaultsProperties;
import com.electricistacat.backend.domain.enums.ChecklistStatus;
import com.electricistacat.backend.domain.model.CalcProfile;
import com.electricistacat.backend.domain.model.ChecklistItem;
import com.electricistacat.backend.domain.model.EarthingSystem;
import com.electricistacat.backend.domain.model.Project;
import com.electricistacat.backend.repository.CalcProfileRepository;
import com.electricistacat.backend.repository.ChecklistItemRepository;
import com.electricistacat.backend.repository.EarthingSystemRepository;
import com.electricistacat.backend.repository.MaterialRepository;
import com.electricistacat.backend.repository.ProjectRepository;
import com.electricistacat.backend.web.dto.BomItemResponse;
import com.electricistacat.backend.web.dto.ChecklistItemRequest;
import com.electricistacat.backend.web.dto.ChecklistItemResponse;
import com.electricistacat.backend.web.dto.ProjectRequest;
import com.electricistacat.backend.web.dto.ProjectResponse;
import com.electricistacat.backend.web.error.ResourceNotFoundException;
import com.electricistacat.backend.web.mapper.ChecklistItemMapper;
import com.electricistacat.backend.web.mapper.MaterialMapper;
import com.electricistacat.backend.web.mapper.ProjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CalcProfileRepository calcProfileRepository;
    private final ProjectMapper projectMapper;
    private final AppDefaultsProperties defaultsProperties;
    private final EarthingSystemRepository earthingSystemRepository;
    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;
    private final ChecklistItemRepository checklistItemRepository;
    private final ChecklistItemMapper checklistItemMapper;
    private final String defaultProfileCode;

    public ProjectService(
            ProjectRepository projectRepository,
            CalcProfileRepository calcProfileRepository,
            ProjectMapper projectMapper,
            AppDefaultsProperties defaultsProperties,
            EarthingSystemRepository earthingSystemRepository,
            MaterialRepository materialRepository,
            MaterialMapper materialMapper,
            ChecklistItemRepository checklistItemRepository,
            ChecklistItemMapper checklistItemMapper,
            @Value("${standards.default-profile-code:}") String defaultProfileCode) {
        this.projectRepository = projectRepository;
        this.calcProfileRepository = calcProfileRepository;
        this.projectMapper = projectMapper;
        this.defaultsProperties = defaultsProperties;
        this.earthingSystemRepository = earthingSystemRepository;
        this.materialRepository = materialRepository;
        this.materialMapper = materialMapper;
        this.checklistItemRepository = checklistItemRepository;
        this.checklistItemMapper = checklistItemMapper;
        this.defaultProfileCode = defaultProfileCode;
    }

    public ProjectResponse createProject(ProjectRequest request) {
        Project project = projectMapper.toEntity(request);
        applyDefaults(project, request);
        project.setCalcProfile(resolveProfile(request.getCalcProfileCode()));
        Project saved = projectRepository.save(project);
        ensureEarthingSystem(saved);
        seedDefaultChecklist(saved);
        return projectMapper.toResponse(saved);
    }

    public ProjectResponse getProject(Long id) {
        return projectMapper.toResponse(loadProject(id));
    }

    public Project loadProject(Long id) {
        return projectRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
    }

    public List<BomItemResponse> getBillOfMaterials(Long projectId) {
        loadProject(projectId);
        return materialRepository.findByProjectId(projectId).stream()
                .map(materialMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ChecklistItemResponse> listChecklist(Long projectId) {
        loadProject(projectId);
        return checklistItemRepository.findByProjectId(projectId).stream()
                .map(checklistItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ChecklistItemResponse addChecklistItem(Long projectId, ChecklistItemRequest request) {
        Project project = loadProject(projectId);
        ChecklistItem item = checklistItemMapper.toEntity(request);
        item.setProject(project);
        ChecklistItem saved = checklistItemRepository.save(item);
        return checklistItemMapper.toResponse(saved);
    }

    private void applyDefaults(Project project, ProjectRequest request) {
        if (project.getCosPhi() == null) {
            project.setCosPhi(defaultsProperties.getDefault().getCosphi());
        }
        if (project.getEfficiency() == null) {
            project.setEfficiency(defaultsProperties.getDefault().getEfficiency());
        }
        if (project.getNominalVoltage() == null) {
            project.setNominalVoltage(project.getVoltageSystemType().name().contains("THREE")
                    ? defaultsProperties.getDefault().getVoltageThreePhase()
                    : defaultsProperties.getDefault().getVoltageSinglePhase());
        }
    }

    private CalcProfile resolveProfile(String calcProfileCode) {
        String code = Optional.ofNullable(calcProfileCode)
                .filter(c -> !c.isBlank())
                .orElse(defaultProfileCode);
        if (code != null && !code.isBlank()) {
            return calcProfileRepository
                    .findByCode(code)
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil de cálculo no encontrado"));
        }
        return calcProfileRepository.findAll().stream().findFirst().orElse(null);
    }

    private void ensureEarthingSystem(Project project) {
        earthingSystemRepository
                .findByProjectId(project.getId())
                .orElseGet(() -> {
                    EarthingSystem system = new EarthingSystem();
                    system.setProject(project);
                    system.setConfiguration("TT - Pozos con jabalina");
                    system.setTargetResistance(10.0);
                    system.setImprovementActions("Verificar humedad del terreno y agregar sales si es necesario.");
                    return earthingSystemRepository.save(system);
                });
    }

    private void seedDefaultChecklist(Project project) {
        if (!checklistItemRepository.findByProjectId(project.getId()).isEmpty()) {
            return;
        }
        ChecklistItem visualInspection = new ChecklistItem();
        visualInspection.setProject(project);
        visualInspection.setDescription("Verificación visual del tablero y etiquetado IRAM");
        visualInspection.setStatus(ChecklistStatus.PENDING);
        ChecklistItem insulationTest = new ChecklistItem();
        insulationTest.setProject(project);
        insulationTest.setDescription("Prueba de aislamiento según IEC 60364");
        insulationTest.setStatus(ChecklistStatus.PENDING);
        checklistItemRepository.save(visualInspection);
        checklistItemRepository.save(insulationTest);
    }
}
