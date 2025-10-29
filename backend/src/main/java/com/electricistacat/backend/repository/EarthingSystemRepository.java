package com.electricistacat.backend.repository;

import com.electricistacat.backend.domain.model.EarthingSystem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EarthingSystemRepository extends JpaRepository<EarthingSystem, Long> {
    Optional<EarthingSystem> findByProjectId(Long projectId);
}
