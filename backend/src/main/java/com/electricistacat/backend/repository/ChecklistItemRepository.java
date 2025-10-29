package com.electricistacat.backend.repository;

import com.electricistacat.backend.domain.model.ChecklistItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
    List<ChecklistItem> findByProjectId(Long projectId);
}
