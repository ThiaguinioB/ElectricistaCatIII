package com.electricistacat.backend.repository;

import com.electricistacat.backend.domain.model.Component;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComponentRepository extends JpaRepository<Component, Long> {
}
