package com.electricistacat.backend.config;

import com.electricistacat.backend.domain.model.CalcProfile;
import com.electricistacat.backend.repository.CalcProfileRepository;
import com.electricistacat.backend.standards.StandardsService;
import com.electricistacat.backend.standards.model.CalcProfileDefinition;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CalcProfileInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalcProfileInitializer.class);

    private final StandardsService standardsService;
    private final CalcProfileRepository repository;

    public CalcProfileInitializer(StandardsService standardsService, CalcProfileRepository repository) {
        this.standardsService = standardsService;
        this.repository = repository;
    }

    @PostConstruct
    public void syncProfiles() {
        for (CalcProfileDefinition definition : standardsService.getProfiles()) {
            repository
                    .findByCode(definition.getCode())
                    .orElseGet(() -> {
                        CalcProfile profile = new CalcProfile();
                        profile.setCode(definition.getCode());
                        profile.setName(definition.getName());
                        profile.setDescription(definition.getDescription());
                        profile.setStandardReferences(String.join(", ", definition.getStandardReferences()));
                        profile.setLightingVoltageDropLimit(definition.getVoltageDropLimits().getOrDefault("lighting", 3.0));
                        profile.setGeneralVoltageDropLimit(definition.getVoltageDropLimits().getOrDefault("general", 5.0));
                        LOGGER.info("Seeding calc profile {}", profile.getCode());
                        return repository.save(profile);
                    });
        }
    }
}
