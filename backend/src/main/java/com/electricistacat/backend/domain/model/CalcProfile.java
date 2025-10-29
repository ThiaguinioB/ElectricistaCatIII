package com.electricistacat.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "calc_profiles")
@Getter
@Setter
public class CalcProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "standard_references", columnDefinition = "text")
    private String standardReferences;

    @Column(name = "lighting_voltage_drop_limit", nullable = false)
    private Double lightingVoltageDropLimit;

    @Column(name = "general_voltage_drop_limit", nullable = false)
    private Double generalVoltageDropLimit;
}
