package com.electricistacat.backend.repository;

import com.electricistacat.backend.domain.model.Circuit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CircuitRepository extends JpaRepository<Circuit, Long> {
    List<Circuit> findByPanelProjectId(Long projectId);

    List<Circuit> findByPanelId(Long panelId);
}
