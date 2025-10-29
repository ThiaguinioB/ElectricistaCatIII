package com.electricistacat.backend.web.rest;

import com.electricistacat.backend.service.CircuitService;
import com.electricistacat.backend.web.dto.CircuitRequest;
import com.electricistacat.backend.web.dto.CircuitResponse;
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
@RequestMapping("/api/panels/{panelId}/circuits")
public class CircuitController {

    private final CircuitService circuitService;

    public CircuitController(CircuitService circuitService) {
        this.circuitService = circuitService;
    }

    @PostMapping
    public ResponseEntity<CircuitResponse> createCircuit(
            @PathVariable Long panelId, @Valid @RequestBody CircuitRequest request) {
        CircuitResponse response = circuitService.addCircuit(panelId, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CircuitResponse>> listCircuits(@PathVariable Long panelId) {
        return ResponseEntity.ok(circuitService.listByPanel(panelId));
    }
}
