package com.electricistacat.backend.repository;

import com.electricistacat.backend.domain.model.Measurement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    List<Measurement> findByEarthingSystemId(Long earthingSystemId);
}
