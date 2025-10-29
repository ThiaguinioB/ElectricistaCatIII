package com.electricistacat.backend.repository;

import com.electricistacat.backend.domain.model.Cable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CableRepository extends JpaRepository<Cable, Long> {
}
