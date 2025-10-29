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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wiring_routes")
@Getter
@Setter
public class WiringRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circuit_id")
    private Circuit circuit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "origin_point")
    private String originPoint;

    @Column(name = "destination_point")
    private String destinationPoint;

    @Column(name = "routing_notes", columnDefinition = "text")
    private String routingNotes;

    private Double length;

    @Column(name = "conduit_type")
    private String conduitType;

    private String labeling;
}
