package com.electricistacat.backend.web.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CalcProfileResponse {
    String code;
    String name;
    String description;
    List<String> standardReferences;
    Double lightingVoltageDropLimit;
    Double generalVoltageDropLimit;
}
