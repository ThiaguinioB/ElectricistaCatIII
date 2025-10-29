package com.electricistacat.backend.repository;

import com.electricistacat.backend.domain.model.WiringRoute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WiringRouteRepository extends JpaRepository<WiringRoute, Long> {
}
