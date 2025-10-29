package com.electricistacat.backend.web.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private String language;
    private boolean includeSingleLineDiagram = true;
    private boolean includeChecklist = true;
    private boolean includeBom = true;
}
