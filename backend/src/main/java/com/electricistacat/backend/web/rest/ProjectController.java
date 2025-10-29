package com.electricistacat.backend.web.rest;

import com.electricistacat.backend.service.ProjectService;
import com.electricistacat.backend.service.ReportService;
import com.electricistacat.backend.web.dto.BomItemResponse;
import com.electricistacat.backend.web.dto.ChecklistItemRequest;
import com.electricistacat.backend.web.dto.ChecklistItemResponse;
import com.electricistacat.backend.web.dto.ProjectRequest;
import com.electricistacat.backend.web.dto.ProjectResponse;
import com.electricistacat.backend.web.dto.ReportRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ReportService reportService;

    public ProjectController(ProjectService projectService, ReportService reportService) {
        this.projectService = projectService;
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    @GetMapping("/{id}/bom")
    public ResponseEntity<?> getBillOfMaterials(
            @PathVariable Long id, @RequestParam(value = "format", required = false) String format) {
        if ("csv".equalsIgnoreCase(format)) {
            byte[] csv = reportService.generateBomCsv(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-" + id + "-bom.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csv);
        }
        List<BomItemResponse> items = projectService.getBillOfMaterials(id);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}/checklist")
    public ResponseEntity<List<ChecklistItemResponse>> listChecklist(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.listChecklist(id));
    }

    @PostMapping("/{id}/checklist")
    public ResponseEntity<ChecklistItemResponse> addChecklistItem(
            @PathVariable Long id, @Valid @RequestBody ChecklistItemRequest request) {
        return ResponseEntity.status(201).body(projectService.addChecklistItem(id, request));
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<byte[]> generateReport(@PathVariable Long id, @RequestBody ReportRequest request) {
        byte[] pdf = reportService.generateTechnicalDossier(id, request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-" + id + "-dossier.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/single-line")
    public ResponseEntity<String> getSingleLineDiagram(@PathVariable Long id) {
        String svg = reportService.generateSingleLineSvg(id);
        return ResponseEntity.ok().contentType(MediaType.valueOf("image/svg+xml")).body(svg);
    }
}
