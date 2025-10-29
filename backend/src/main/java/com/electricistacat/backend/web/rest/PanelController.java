package com.electricistacat.backend.web.rest;

import com.electricistacat.backend.service.PanelService;
import com.electricistacat.backend.web.dto.PanelRequest;
import com.electricistacat.backend.web.dto.PanelResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/panels")
public class PanelController {

    private final PanelService panelService;

    public PanelController(PanelService panelService) {
        this.panelService = panelService;
    }

    @PostMapping
    public ResponseEntity<PanelResponse> createPanel(
            @PathVariable Long projectId, @Valid @RequestBody PanelRequest request) {
        PanelResponse response = panelService.addPanel(projectId, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PanelResponse>> listPanels(@PathVariable Long projectId) {
        return ResponseEntity.ok(panelService.listPanels(projectId));
    }
}
