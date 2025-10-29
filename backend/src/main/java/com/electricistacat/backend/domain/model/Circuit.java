package com.electricistacat.backend.domain.model;

import com.electricistacat.backend.domain.enums.PhaseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "circuits")
@Getter
@Setter
public class Circuit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_id", nullable = false)
    private Panel panel;

    @Column(nullable = false)
    private String name;

    @Column(name = "load_type")
    private String loadType;

    @Column(name = "demand_power_kw")
    private Double demandPowerKw;

    private Double voltage;

    @Column(name = "cos_phi")
    private Double cosPhi;

    private Double efficiency;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase_type", nullable = false)
    private PhaseType phaseType;

    @Column(name = "length_m")
    private Double lengthMeters;

    @Column(name = "conductor_cross_section")
    private Double conductorCrossSection;

    @Column(name = "conductor_material")
    private String conductorMaterial;

    @Column(name = "conductor_insulation")
    private String conductorInsulation;

    @Column(name = "installation_method")
    private String installationMethod;

    @Column(name = "grouping_factor_count")
    private Integer groupingFactorCount;

    @Column(name = "ambient_temperature")
    private Double ambientTemperature;

    @Column(name = "is_lighting")
    private Boolean lighting;

    @Column(name = "protective_device_curve")
    private String protectiveDeviceCurve;

    @Column(name = "protective_device_rating")
    private Double protectiveDeviceRating;

    @Column(name = "design_current")
    private Double designCurrent;

    @Column(name = "breaker_current")
    private Double breakerCurrent;

    @Column(name = "iz")
    private Double iz;

    @Column(name = "voltage_drop")
    private Double voltageDrop;

    @Column(name = "conductor_colors", columnDefinition = "text")
    private String conductorColors;
}
