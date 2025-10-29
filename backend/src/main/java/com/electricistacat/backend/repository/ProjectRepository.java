package com.electricistacat.backend.repository;

import com.electricistacat.backend.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
