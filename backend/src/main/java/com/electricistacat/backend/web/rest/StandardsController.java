package com.electricistacat.backend.web.rest;

import com.electricistacat.backend.standards.StandardsService;
import com.electricistacat.backend.standards.model.CalcProfileDefinition;
import com.electricistacat.backend.standards.model.CircuitTemplate;
import com.electricistacat.backend.web.dto.CalcProfileResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/standards")
public class StandardsController {

    private final StandardsService standardsService;

    public StandardsController(StandardsService standardsService) {
        this.standardsService = standardsService;
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<CalcProfileResponse>> listProfiles() {
        List<CalcProfileResponse> responses = standardsService.getProfiles().stream()
                .map(this::mapProfile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<CircuitTemplate>> listTemplates() {
        return ResponseEntity.ok(standardsService.listTemplates());
    }

    private CalcProfileResponse mapProfile(CalcProfileDefinition definition) {
        return CalcProfileResponse.builder()
                .code(definition.getCode())
                .name(definition.getName())
                .description(definition.getDescription())
                .standardReferences(definition.getStandardReferences())
                .lightingVoltageDropLimit(definition.getVoltageDropLimits().get("lighting"))
                .generalVoltageDropLimit(definition.getVoltageDropLimits().get("general"))
                .build();
    }
}
