package com.electricistacat.backend.repository;

import com.electricistacat.backend.domain.model.CalcProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalcProfileRepository extends JpaRepository<CalcProfile, Long> {
    Optional<CalcProfile> findByCode(String code);
}
