package com.electricistacat.backend.service;

import com.electricistacat.backend.domain.model.Panel;
import com.electricistacat.backend.domain.model.Project;
import com.electricistacat.backend.repository.PanelRepository;
import com.electricistacat.backend.web.dto.PanelRequest;
import com.electricistacat.backend.web.dto.PanelResponse;
import com.electricistacat.backend.web.error.ResourceNotFoundException;
import com.electricistacat.backend.web.mapper.PanelMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PanelService {

    private final PanelRepository panelRepository;
    private final PanelMapper panelMapper;
    private final ProjectService projectService;

    public PanelService(PanelRepository panelRepository, PanelMapper panelMapper, ProjectService projectService) {
        this.panelRepository = panelRepository;
        this.panelMapper = panelMapper;
        this.projectService = projectService;
    }

    public PanelResponse addPanel(Long projectId, PanelRequest request) {
        Project project = projectService.loadProject(projectId);
        Panel panel = panelMapper.toEntity(request);
        panel.setProject(project);
        Panel saved = panelRepository.save(panel);
        return panelMapper.toResponse(saved);
    }

    public java.util.List<PanelResponse> listPanels(Long projectId) {
        projectService.loadProject(projectId);
        return panelRepository.findByProjectId(projectId).stream()
                .map(panelMapper::toResponse)
                .toList();
    }

    public Panel loadPanel(Long panelId) {
        return panelRepository
                .findById(panelId)
                .orElseThrow(() -> new ResourceNotFoundException("Panel no encontrado"));
    }
}
