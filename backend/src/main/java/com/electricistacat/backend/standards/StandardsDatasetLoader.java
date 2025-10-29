package com.electricistacat.backend.standards;

import com.electricistacat.backend.standards.model.StandardsDataset;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class StandardsDatasetLoader {

    private final ObjectMapper objectMapper;
    private final Resource datasetResource;

    public StandardsDatasetLoader(
            ObjectMapper objectMapper, @Value("${standards.dataset}") Resource datasetResource) {
        this.objectMapper = objectMapper;
        this.datasetResource = datasetResource;
    }

    public StandardsDataset load() {
        try (InputStream inputStream = datasetResource.getInputStream()) {
            return objectMapper.readValue(inputStream, StandardsDataset.class);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load standards dataset", e);
        }
    }
}
