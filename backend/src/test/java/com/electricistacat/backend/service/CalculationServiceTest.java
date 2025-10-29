package com.electricistacat.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.electricistacat.backend.config.AppDefaultsProperties;
import com.electricistacat.backend.standards.StandardsService;
import com.electricistacat.backend.standards.model.BreakerCatalogEntry;
import com.electricistacat.backend.standards.model.CalcProfileDefinition;
import com.electricistacat.backend.standards.model.ConductorProperties;
import com.electricistacat.backend.standards.model.ThermalCapacityEntry;
import com.electricistacat.backend.web.dto.CalculationResponse;
import com.electricistacat.backend.web.dto.IARequest;
import com.electricistacat.backend.web.dto.INRequest;
import com.electricistacat.backend.web.dto.IZRequest;
import com.electricistacat.backend.web.dto.VoltageDropRequest;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculationServiceTest {

    @Mock
    private StandardsService standardsService;

    private CalculationService calculationService;
    private AppDefaultsProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AppDefaultsProperties();
        properties.getDefault().setVoltageSinglePhase(230d);
        properties.getDefault().setVoltageThreePhase(400d);
        properties.getDefault().setCosphi(0.9d);
        properties.getDefault().setEfficiency(0.9d);
        properties.getDefault().setConductorMaterial("CU");
        properties.getDefault().setInstallationMethod("concealed_conduit");
        properties.getVoltageDrop().setLightingLimitPercent(3.0d);
        properties.getVoltageDrop().setGeneralLimitPercent(5.0d);
        calculationService = new CalculationService(standardsService, properties);
    }

    @Test
    void calculateIASinglePhaseUsesApplicationDefaults() {
        IARequest request = new IARequest();
        request.setPowerKw(4.6d);

        CalculationResponse response = calculationService.calculateIA(request);

        double expected = 4600d / (230d * 0.9d * 0.9d);
        assertThat(response.getResult()).isCloseTo(expected, within(1e-6));
        assertThat(response.getUnit()).isEqualTo("A");
        assertThat(response.getMetadata())
                .containsEntry("voltage", 230d)
                .containsEntry("cosPhi", 0.9d)
                .containsEntry("efficiency", 0.9d);
    }

    @Test
    void calculateIAThreePhaseHonoursExplicitParameters() {
        IARequest request = new IARequest();
        request.setPowerKw(15d);
        request.setThreePhase(true);
        request.setVoltage(415d);
        request.setPowerFactor(0.92d);
        request.setEfficiency(0.95d);

        CalculationResponse response = calculationService.calculateIA(request);

        double expected = 15000d / (Math.sqrt(3) * 415d * 0.92d * 0.95d);
        assertThat(response.getResult()).isCloseTo(expected, within(1e-6));
        assertThat(response.getMetadata())
                .containsEntry("voltage", 415d)
                .containsEntry("cosPhi", 0.92d)
                .containsEntry("efficiency", 0.95d);
    }

    @Test
    void calculateINReturnsBreakerMetadata() {
        BreakerCatalogEntry entry = new BreakerCatalogEntry();
        entry.setCurve("C");
        entry.setRating(32d);
        entry.setTolerance(1.13d);
        entry.setStandard("IEC-60898");
        INRequest request = new INRequest();
        request.setDesignCurrent(28d);
        request.setPreferredCurve("C");
        request.setTolerance(1.1d);
        when(standardsService.findBreaker(28d, "C", 1.1d)).thenReturn(Optional.of(entry));

        CalculationResponse response = calculationService.calculateIN(request);

        assertThat(response.getResult()).isEqualTo(32d);
        assertThat(response.getMetadata())
                .containsEntry("curve", "C")
                .containsEntry("rating", 32d)
                .containsEntry("tolerance", 1.13d)
                .containsEntry("standard", "IEC-60898");
    }

    @Test
    void calculateINThrowsWhenBreakerNotFound() {
        INRequest request = new INRequest();
        request.setDesignCurrent(63d);
        when(standardsService.findBreaker(63d, null, null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calculationService.calculateIN(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No breaker");
    }

    @Test
    void calculateIZAppliesCorrectionFactors() {
        ThermalCapacityEntry entry = new ThermalCapacityEntry();
        entry.setStandard("IRAM-2183");
        entry.setInstallationMethod("concealed_conduit");
        entry.setMaterial("CU");
        entry.setInsulation("PVC");
        entry.setCrossSectionMm2(2.5d);
        entry.setIz(24d);
        when(standardsService.findThermalCapacity("concealed_conduit", "CU", "PVC", 2.5d))
                .thenReturn(Optional.of(entry));
        when(standardsService.computeAmbientFactor(40d)).thenReturn(0.91d);
        when(standardsService.computeGroupingFactor(2)).thenReturn(0.8d);

        IZRequest request = new IZRequest();
        request.setInstallationMethod("concealed_conduit");
        request.setCrossSectionMm2(2.5d);
        request.setInsulation("PVC");
        request.setAmbientTemperature(40d);
        request.setGroupingCount(2);

        CalculationResponse response = calculationService.calculateIZ(request);

        double expected = 24d * 0.91d * 0.8d;
        assertThat(response.getResult()).isCloseTo(expected, within(1e-6));
        assertThat(response.getMetadata())
                .containsEntry("baseIz", 24d)
                .containsEntry("ambientFactor", 0.91d)
                .containsEntry("groupingFactor", 0.8d)
                .containsEntry("standard", "IRAM-2183");
    }

    @Test
    void calculateIZThrowsWhenDatasetMissing() {
        when(standardsService.findThermalCapacity("tray", "CU", null, 10d))
                .thenReturn(Optional.empty());
        IZRequest request = new IZRequest();
        request.setInstallationMethod("tray");
        request.setMaterial("CU");
        request.setCrossSectionMm2(10d);

        assertThatThrownBy(() -> calculationService.calculateIZ(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No IZ data");
    }

    @Test
    void calculateVoltageDropComputesMagnitudeAndMetadata() {
        ConductorProperties conductor = new ConductorProperties();
        conductor.setMaterial("CU");
        conductor.setResistivityOhmPerKm(0.0181d);
        conductor.setReactanceOhmPerKm(0.00008d);
        when(standardsService.findConductor("CU")).thenReturn(Optional.of(conductor));

        VoltageDropRequest request = new VoltageDropRequest();
        request.setCurrent(25d);
        request.setLengthMeters(35d);
        request.setCrossSectionMm2(4d);
        request.setMaterial("CU");
        request.setPowerFactor(0.95d);

        CalculationResponse response = calculationService.calculateVoltageDrop(request);

        double resistancePerKm = 0.0181d / 4d;
        double reactancePerKm = 0.00008d;
        double expected = 2d * 25d * (35d / 1000d)
                * (resistancePerKm * 0.95d + reactancePerKm * Math.sqrt(1 - Math.pow(0.95d, 2)));
        assertThat(response.getResult()).isCloseTo(expected, within(1e-9));
        assertThat(response.getMetadata())
                .containsEntry("material", "CU")
                .containsEntry("resistancePerKm", resistancePerKm)
                .containsEntry("reactancePerKm", reactancePerKm)
                .containsEntry("voltage", 230d);
        assertThat((Double) response.getMetadata().get("percent")).isCloseTo(
                (expected / 230d) * 100d, within(1e-6));
    }

    @Test
    void resolveVoltageDropLimitPrefersProfileValue() {
        CalcProfileDefinition profile = new CalcProfileDefinition();
        profile.setVoltageDropLimits(Map.of("lighting", 2.5d, "general", 4.0d));

        double lightingLimit = calculationService.resolveVoltageDropLimit(true, profile);
        double generalLimit = calculationService.resolveVoltageDropLimit(false, profile);

        assertThat(lightingLimit).isEqualTo(2.5d);
        assertThat(generalLimit).isEqualTo(4.0d);
    }

    @Test
    void resolveVoltageDropLimitFallsBackToDefaultsWhenMissing() {
        CalcProfileDefinition profile = new CalcProfileDefinition();
        profile.setVoltageDropLimits(Map.of("lighting", 2.0d));

        double generalLimit = calculationService.resolveVoltageDropLimit(false, profile);
        double lightingLimit = calculationService.resolveVoltageDropLimit(true, null);

        assertThat(generalLimit).isEqualTo(5.0d);
        assertThat(lightingLimit).isEqualTo(3.0d);
    }
}
