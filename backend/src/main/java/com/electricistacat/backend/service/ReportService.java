package com.electricistacat.backend.service;

import com.electricistacat.backend.domain.model.Circuit;
import com.electricistacat.backend.domain.model.Material;
import com.electricistacat.backend.domain.model.Panel;
import com.electricistacat.backend.domain.model.Project;
import com.electricistacat.backend.repository.CircuitRepository;
import com.electricistacat.backend.repository.MaterialRepository;
import com.electricistacat.backend.repository.PanelRepository;
import com.electricistacat.backend.web.dto.ChecklistItemResponse;
import com.electricistacat.backend.web.dto.MeasurementResponse;
import com.electricistacat.backend.web.dto.ReportRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ProjectService projectService;
    private final PanelRepository panelRepository;
    private final CircuitRepository circuitRepository;
    private final MaterialRepository materialRepository;
    private final EarthingService earthingService;

    public ReportService(
            ProjectService projectService,
            PanelRepository panelRepository,
            CircuitRepository circuitRepository,
            MaterialRepository materialRepository,
            EarthingService earthingService) {
        this.projectService = projectService;
        this.panelRepository = panelRepository;
        this.circuitRepository = circuitRepository;
        this.materialRepository = materialRepository;
        this.earthingService = earthingService;
    }

    public byte[] generateTechnicalDossier(Long projectId, ReportRequest request) {
        Project project = projectService.loadProject(projectId);
        List<Panel> panels = panelRepository.findByProjectId(projectId);
        List<Circuit> circuits = circuitRepository.findByPanelProjectId(projectId);
        List<Material> materials = materialRepository.findByProjectId(projectId);
        List<MeasurementResponse> measurements = earthingService.listMeasurements(projectId);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 40;
                float y = page.getMediaBox().getHeight() - margin;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Expediente Técnico - Proyecto " + project.getName());
                contentStream.endText();
                y -= 30;

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                y = writeParagraph(contentStream, margin, y,
                        "Cliente: " + Optional.ofNullable(project.getClientName()).orElse("N/D") + " | Dirección: "
                                + Optional.ofNullable(project.getAddress()).orElse("N/D"));
                y = writeParagraph(contentStream, margin, y,
                        String.format(Locale.getDefault(),
                                "Sistema: %s - Vn: %.1f V - cosφ: %.2f - η: %.2f",
                                project.getVoltageSystemType(), project.getNominalVoltage(), project.getCosPhi(),
                                project.getEfficiency()));
                y = writeParagraph(contentStream, margin, y, "Perfil normativo: "
                        + (project.getCalcProfile() != null ? project.getCalcProfile().getCode() : "N/D"));

                y = writeSectionTitle(contentStream, margin, y - 10, "Tableros y circuitos");
                for (Panel panel : panels) {
                    y = writeParagraph(contentStream, margin, y,
                            "Panel " + panel.getName() + " (" + panel.getPanelType() + ")");
                    for (Circuit circuit : circuits.stream()
                            .filter(c -> c.getPanel().getId().equals(panel.getId()))
                            .toList()) {
                        y = writeParagraph(contentStream, margin + 10, y,
                                String.format(Locale.getDefault(),
                                        "Circuito %s - IA: %.2f A | IN: %.2f A | IZ: %.2f A | ΔV: %.2f%%",
                                        circuit.getName(),
                                        Optional.ofNullable(circuit.getDesignCurrent()).orElse(0d),
                                        Optional.ofNullable(circuit.getBreakerCurrent()).orElse(0d),
                                        Optional.ofNullable(circuit.getIz()).orElse(0d),
                                        Optional.ofNullable(circuit.getVoltageDrop()).orElse(0d)));
                    }
                }

                y = writeSectionTitle(contentStream, margin, y - 10, "Materiales");
                for (Material material : materials) {
                    y = writeParagraph(contentStream, margin, y,
                            material.getDescription() + " - " + material.getQuantity() + material.getUnit());
                }

                if (request.isIncludeChecklist()) {
                    y = writeSectionTitle(contentStream, margin, y - 10, "Checklist");
                    for (ChecklistItemResponse item : projectService.listChecklist(projectId)) {
                        y = writeParagraph(contentStream, margin, y,
                                "[" + item.getStatus() + "] " + item.getDescription());
                    }
                }

                if (!measurements.isEmpty()) {
                    y = writeSectionTitle(contentStream, margin, y - 10, "Mediciones de puesta a tierra");
                    for (MeasurementResponse measurement : measurements) {
                        y = writeParagraph(contentStream, margin, y,
                                String.format(Locale.getDefault(),
                                        "%.2f Ω - %s", measurement.getResistance(), measurement.getMeasuredAt()));
                    }
                }
            }
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        }
    }

    public byte[] generateBomCsv(Long projectId) {
        Project project = projectService.loadProject(projectId);
        List<Material> materials = materialRepository.findByProjectId(project.getId());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                CSVPrinter printer = new CSVPrinter(
                        new OutputStreamWriter(baos, StandardCharsets.UTF_8),
                        CSVFormat.DEFAULT.withHeader("Descripción", "Unidad", "Cantidad", "Norma"))) {
            for (Material material : materials) {
                printer.printRecord(
                        material.getDescription(), material.getUnit(), material.getQuantity(), material.getReferenceStandard());
            }
            printer.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el CSV", e);
        }
    }

    public String generateSingleLineSvg(Long projectId) {
        Project project = projectService.loadProject(projectId);
        List<Circuit> circuits = circuitRepository.findByPanelProjectId(projectId);
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"800\" height=\"")
                .append(120 + circuits.size() * 40)
                .append("\">");
        svg.append("<rect width='100%' height='100%' fill='white'/>");
        svg.append("<text x='20' y='30' font-family='Arial' font-size='18'>Unifilar - ")
                .append(project.getName())
                .append("</text>");
        int y = 80;
        for (Circuit circuit : circuits) {
            svg.append("<line x1='100' y1='").append(y).append("' x2='400' y2='").append(y)
                    .append("' stroke='black' stroke-width='2'/>");
            svg.append("<circle cx='100' cy='").append(y).append("' r='5' fill='black'/>");
            svg.append("<text x='420' y='").append(y + 5)
                    .append("' font-family='Arial' font-size='12'>")
                    .append(circuit.getName()).append(" - ")
                    .append(Optional.ofNullable(circuit.getBreakerCurrent()).orElse(0d)).append("A</text>");
            y += 40;
        }
        svg.append("</svg>");
        return svg.toString();
    }

    private float writeParagraph(PDPageContentStream contentStream, float margin, float y, String text)
            throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, y);
        contentStream.showText(text);
        contentStream.endText();
        return y - 18;
    }

    private float writeSectionTitle(PDPageContentStream contentStream, float margin, float y, String title)
            throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, y);
        contentStream.showText(title);
        contentStream.endText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        return y - 20;
    }
}
