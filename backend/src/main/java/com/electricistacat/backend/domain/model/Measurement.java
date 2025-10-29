package com.electricistacat.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "measurements")
@Getter
@Setter
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "earthing_system_id", nullable = false)
    private EarthingSystem earthingSystem;

    @Column(nullable = false)
    private Double resistance;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    private String instrument;

    @Column(columnDefinition = "text")
    private String notes;
}
