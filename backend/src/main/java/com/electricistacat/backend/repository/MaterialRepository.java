package com.electricistacat.backend.repository;

import com.electricistacat.backend.domain.model.Material;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByProjectId(Long projectId);
}
