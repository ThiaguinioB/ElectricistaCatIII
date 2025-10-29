package com.electricistacat.backend.web.rest;

import com.electricistacat.backend.service.CalculationService;
import com.electricistacat.backend.web.dto.CalculationResponse;
import com.electricistacat.backend.web.dto.IARequest;
import com.electricistacat.backend.web.dto.INRequest;
import com.electricistacat.backend.web.dto.IZRequest;
import com.electricistacat.backend.web.dto.VoltageDropRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculations")
public class CalculationController {

    private final CalculationService calculationService;

    public CalculationController(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @PostMapping("/ia")
    public ResponseEntity<CalculationResponse> calculateIa(@Valid @RequestBody IARequest request) {
        return ResponseEntity.ok(calculationService.calculateIA(request));
    }

    @PostMapping("/in")
    public ResponseEntity<CalculationResponse> calculateIn(@Valid @RequestBody INRequest request) {
        return ResponseEntity.ok(calculationService.calculateIN(request));
    }

    @PostMapping("/iz")
    public ResponseEntity<CalculationResponse> calculateIz(@Valid @RequestBody IZRequest request) {
        return ResponseEntity.ok(calculationService.calculateIZ(request));
    }

    @PostMapping("/voltage-drop")
    public ResponseEntity<CalculationResponse> calculateVoltageDrop(@Valid @RequestBody VoltageDropRequest request) {
        return ResponseEntity.ok(calculationService.calculateVoltageDrop(request));
    }
}
