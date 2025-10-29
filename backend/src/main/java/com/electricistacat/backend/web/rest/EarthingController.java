package com.electricistacat.backend.web.rest;

import com.electricistacat.backend.service.EarthingService;
import com.electricistacat.backend.web.dto.EarthingMeasurementRequest;
import com.electricistacat.backend.web.dto.MeasurementResponse;
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
@RequestMapping("/api/earthing")
public class EarthingController {

    private final EarthingService earthingService;

    public EarthingController(EarthingService earthingService) {
        this.earthingService = earthingService;
    }

    @PostMapping("/{projectId}/measurements")
    public ResponseEntity<MeasurementResponse> addMeasurement(
            @PathVariable Long projectId, @Valid @RequestBody EarthingMeasurementRequest request) {
        MeasurementResponse response = earthingService.logMeasurement(projectId, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{projectId}/measurements")
    public ResponseEntity<List<MeasurementResponse>> listMeasurements(@PathVariable Long projectId) {
        return ResponseEntity.ok(earthingService.listMeasurements(projectId));
    }
}
