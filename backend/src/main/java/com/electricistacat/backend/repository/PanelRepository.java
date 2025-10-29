package com.electricistacat.backend.repository;

import com.electricistacat.backend.domain.model.Panel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PanelRepository extends JpaRepository<Panel, Long> {
    List<Panel> findByProjectId(Long projectId);
}
